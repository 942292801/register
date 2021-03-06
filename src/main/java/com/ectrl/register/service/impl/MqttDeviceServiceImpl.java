package com.ectrl.register.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ectrl.register.commons.CRC16Util;
import com.ectrl.register.commons.RSAUtil;
import com.ectrl.register.domain.MqttDeviceEntity;
import com.ectrl.register.dto.BaseResult;
import com.ectrl.register.mapper.MqttDeviceMapper;
import com.ectrl.register.service.MqttDeviceService;
import com.ectrl.register.service.MqttWpapskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

/**
*@Author: Wen zhenwei
*@date: 2020/3/30 14:53
*@Description: 申请设备数字证书
 *@Param:
*@return:
*/
@Service
public class MqttDeviceServiceImpl extends ServiceImpl<MqttDeviceMapper, MqttDeviceEntity> implements MqttDeviceService {



    @Autowired
    MqttWpapskService mqttWpapskService;

    @Override
    public BaseResult applyDeviceCA(String enCertId, String publicKey)  {
        try {
            //解密
            String CertId =  mqttWpapskService.decryptString(enCertId);
            if (StringUtils.isEmpty(CertId) ){
                return BaseResult.fail("参数certid解密失败 请检查该参数");
            }
            if (CertId.length() != 32){
                return BaseResult.fail("参数certid格式错误 请检查该参数");
            }
            String sn = CertId.substring(0,16);
            String mac = CertId.substring(16,28);
            String crc = CertId.substring(28,32);
            //crc是否符合校验
            if (!crc.equals(CRC16Util.calcCrc16((sn+mac).getBytes())))
            {
                //不通过就不用查数据库 浪费资源
                return BaseResult.fail("参数certid格式错误 请检查该参数");

            }

            QueryWrapper<MqttDeviceEntity> queryWrapper = new QueryWrapper<>();
            //构造数据库语句
            queryWrapper.eq("sn",sn);
            queryWrapper.eq("mac",mac);
            queryWrapper.eq("crc",crc);
            MqttDeviceEntity mqttDeviceEntity = this.getOne(queryWrapper);
            if (mqttDeviceEntity == null)
            {
                return BaseResult.fail("参数certid未绑定账户 请先绑定账户");
            }
            if (!StringUtils.isEmpty(mqttDeviceEntity.getSign()) ){
                //存在签名
                //是否过期
                if (mqttDeviceEntity.getExpired().after(new Date())){
                    //没过期 直接返回失败
                    return BaseResult.fail("已申请数字证书 不能重复申请");
                }else {
                    //过期 重新注册
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.YEAR, 2);//增加一年
                    mqttDeviceEntity.setExpired(cal.getTime());
                }

            }
            mqttDeviceEntity.setRandom((int)((Math.random()*9+1)*100));

            // 注册设备
            String sign = Base64.getEncoder().encodeToString( (mqttDeviceEntity.getSn()+mqttDeviceEntity.getMac()+mqttDeviceEntity.getCrc()+mqttDeviceEntity.getCreated().getTime()+mqttDeviceEntity.getExpired().getTime()+mqttDeviceEntity.getRandom()).getBytes("UTF-8"));
            mqttDeviceEntity.setSign(sign);
            //加密返回数据
            String CA =String.format("certid=%s&created=%s&expired=%s&sign=%s",mqttDeviceEntity.getSn()+mqttDeviceEntity.getMac()+mqttDeviceEntity.getCrc(),mqttDeviceEntity.getCreated().getTime(),mqttDeviceEntity.getExpired().getTime(),mqttDeviceEntity.getSign());
            String enCA = RSAUtil.encipher(CA,publicKey.replaceAll(" +","+"));
            if (StringUtils.isEmpty(enCA))
            {
                return BaseResult.fail("参数publicKey错误 请检查该参数");
            }
            this.update(mqttDeviceEntity,queryWrapper);

            return BaseResult.success("设备申请数字证书成功",enCA);
        }catch (Exception e){
            e.printStackTrace();
            return BaseResult.fail("发生未知错误！");
        }
    }

}
