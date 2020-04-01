package com.ectrl.register.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ectrl.register.commons.ConstantUtil;
import com.ectrl.register.commons.RSAUtil;
import com.ectrl.register.domain.MqttWpapskEntity;
import com.ectrl.register.mapper.MqttWpapskMapper;
import com.ectrl.register.service.MqttWpapskService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/**
*@Author: Wen zhenwei
*@date: 2020/3/17 10:30
*@Description: RSA数据库操作
*@Param:
*@return:
*/
@Service
public class MqttWpapskServiceImpl extends ServiceImpl<MqttWpapskMapper, MqttWpapskEntity> implements MqttWpapskService {



    @Override
    public MqttWpapskEntity getKey() {
        //条件构造器
        QueryWrapper<MqttWpapskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ConstantUtil.MYSQL_SERVER_NAME,ConstantUtil.SERVER_NAME_REGISTER);
        MqttWpapskEntity mqttWpapskEntity = this.getOne(queryWrapper);
        return mqttWpapskEntity;


    }

    @SneakyThrows
    @Override
    public Boolean updateKey() {
        QueryWrapper<MqttWpapskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ConstantUtil.MYSQL_SERVER_NAME,ConstantUtil.SERVER_NAME_REGISTER);
        RSAUtil.KeyPairInfo keyPairInfo = RSAUtil.getKeyPair();
        MqttWpapskEntity mqttWpapskEntity = new MqttWpapskEntity();
        mqttWpapskEntity.setPublicKey(keyPairInfo.getPublicKey());
        mqttWpapskEntity.setPrivateKey(keyPairInfo.getPrivateKey());
        return update(mqttWpapskEntity,queryWrapper);

    }

    @Override
    public String encryptString(String content) throws Exception {
        try {

            //加密
        QueryWrapper<MqttWpapskEntity> queryWrapper = new QueryWrapper<>();
        //构造数据库语句
        queryWrapper.eq(ConstantUtil.MYSQL_SERVER_NAME,ConstantUtil.SERVER_NAME_REGISTER);
        //获取实体类privatekey和publickey
        MqttWpapskEntity mqttWpapskEntity =  this.getOne(queryWrapper);
        //用公钥加密
        return RSAUtil.encipher(content.replaceAll(" +", "+"),mqttWpapskEntity.getPublicKey());
        }catch (Exception ex)
        {

            return  null;
        }
    }

    @Override
    public String decryptString(String content) throws Exception {
        try {
            //解密
            QueryWrapper<MqttWpapskEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(ConstantUtil.MYSQL_SERVER_NAME, ConstantUtil.SERVER_NAME_REGISTER);
            //获取实体类privatekey和publickey
            MqttWpapskEntity mqttWpapskEntity = this.getOne(queryWrapper);
            //用私钥解密
            String str = RSAUtil.decipher(content.replaceAll(" +", "+"), mqttWpapskEntity.getPrivateKey());
            return str;
        }catch (Exception ex)
        {
            return  null;
        }
    }







}
