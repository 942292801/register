package com.ectrl.register.controller;


import com.ectrl.register.dto.BaseResult;
import com.ectrl.register.service.MqttDeviceService;
import com.ectrl.register.service.MqttServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
*@Author: Wen zhenwei
*@date: 2020/3/26 17:04
*@Description: 申请数字证书
*@Param:
*@return:
*/
@RestController
@RequestMapping("/applyca")
public class ApplyCAController {

    @Autowired
    private MqttServerService mqttServerService;

    @Autowired
    private MqttDeviceService mqttDeviceService;

    @RequestMapping("/server")
    public BaseResult applyServerCA(@RequestParam(value = "certid") String certId, @RequestParam(value = "key") String key) throws Exception {

        String CA = mqttServerService.applyServerCA(certId,key);
        if (StringUtils.isEmpty(CA)){
            return BaseResult.fail("申请主服务器数字证书失败");
        }else {
            return BaseResult.success("申请主服务器数字证书成功",CA);
        }

    }

    @RequestMapping("/device")
    public BaseResult applyDeviceCA(@RequestParam(value = "certid") String certId, @RequestParam(value = "key") String key) throws Exception {

        String CA = mqttDeviceService.applyDeviceCA(certId,key);
        if (StringUtils.isEmpty(CA)){
            return BaseResult.fail("申请设备数字证书失败");
        }else {
            return BaseResult.success("申请设备数字证书成功",CA);
        }

    }


}