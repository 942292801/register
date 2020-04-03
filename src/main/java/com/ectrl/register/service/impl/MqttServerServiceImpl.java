package com.ectrl.register.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ectrl.register.commons.CRC16Util;
import com.ectrl.register.commons.RSAUtil;
import com.ectrl.register.domain.MqttServerEntity;
import com.ectrl.register.dto.BaseResult;
import com.ectrl.register.mapper.MqttServerMapper;
import com.ectrl.register.service.MqttServerService;
import com.ectrl.register.service.MqttWpapskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

/**
*@Author: Wen zhenwei
*@date: 2020/3/26 17:21
*@Description: 申请主服务器数字证书
*@Param:
*@return:
*/
@Service
public class MqttServerServiceImpl extends ServiceImpl<MqttServerMapper, MqttServerEntity> implements MqttServerService {

    @Autowired
    MqttWpapskService mqttWpapskService;

    @Override
    public BaseResult applyServerCA(String enCertId, String publicKey)  {
        try{
            //解密
            String CertId =  mqttWpapskService.decryptString(enCertId);
            if (StringUtils.isEmpty(CertId) ){
                return BaseResult.fail("参数certid解密失败 请检查该参数");
            }
            if (CertId.length() != 34) {
                return BaseResult.fail("参数certid格式错误 请检查该参数");
            }
            String sn = CertId.substring(0,18);
            String mac = CertId.substring(18,30);
            String crc = CertId.substring(30,34);
            //crc是否符合校验
            if (!crc.equals(CRC16Util.calcCrc16((sn+mac).getBytes())))
            {
                //不通过就不用查数据库 浪费资源
                return BaseResult.fail("参数certid格式错误 请检查该参数");
            }
            QueryWrapper<MqttServerEntity> queryWrapper = new QueryWrapper<>();
            //构造数据库语句
            queryWrapper.eq("sn",sn);
            queryWrapper.eq("mac",mac);
            queryWrapper.eq("crc",crc);
            MqttServerEntity mqttServerEntity = this.getOne(queryWrapper);
            if (mqttServerEntity == null)
            {
                //数据库不存在
                return BaseResult.fail("参数certid未绑定账户 请先绑定账户");
            }
            if (!StringUtils.isEmpty(mqttServerEntity.getSign()) ){
                //存在签名
                //是否过期
                if (mqttServerEntity.getExpired().after(new Date())){
                    //没过期 直接返回失败
                     return BaseResult.fail("已申请数字证书 不能重复申请");
                }else {
                    //过期 重新注册
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.YEAR, 2);//增加一年
                    mqttServerEntity.setExpired(cal.getTime());
                }

            }

            mqttServerEntity.setRandom((int)((Math.random()*9+1)*100));
            // 编码
            String sign = Base64.getEncoder().encodeToString( (mqttServerEntity.getSn()+mqttServerEntity.getMac()+mqttServerEntity.getCrc()+mqttServerEntity.getCreated().getTime()+mqttServerEntity.getExpired().getTime()+mqttServerEntity.getRandom()).getBytes("UTF-8"));
            mqttServerEntity.setSign(sign);
            //加密返回数据
            String CA =String.format("certid=%s&created=%s&expired=%s&sign=%s",mqttServerEntity.getSn()+mqttServerEntity.getMac()+mqttServerEntity.getCrc(),mqttServerEntity.getCreated().getTime(),mqttServerEntity.getExpired().getTime(),mqttServerEntity.getSign());
            String enCA =RSAUtil.encipher(CA,publicKey.replaceAll(" +","+"));
            if (StringUtils.isEmpty(enCA))
            {
                return BaseResult.fail("参数publicKey错误 请检查该参数");
            }
            this.update(mqttServerEntity,queryWrapper);

            return BaseResult.success("主服务器申请数字证书成功",enCA);
        }catch (Exception e){
            e.printStackTrace();
            return BaseResult.fail("发生未知错误！");
        }


    }



}
