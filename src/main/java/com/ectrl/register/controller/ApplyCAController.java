package com.ectrl.register.controller;


import com.ectrl.register.dto.BaseResult;
import com.ectrl.register.service.MqttDeviceService;
import com.ectrl.register.service.MqttServerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@Api(value = "申请数字证书的信息接口",tags = "申请数字证书的信息接口")
public class ApplyCAController {

    @Autowired
    private MqttServerService mqttServerService;

    @Autowired
    private MqttDeviceService mqttDeviceService;

    @RequestMapping(value = "/server",method = RequestMethod.POST)
    @ApiOperation(value = "主服务器申请数字证书",notes = "主服务器申请数字证书,参数certid：产品序列信息，参数publickey：本地公钥",httpMethod = "POST")
    public BaseResult applyServerCA(@RequestParam(value = "certid") String certId, @RequestParam(value = "publickey") String publickey) throws Exception {

        return mqttServerService.applyServerCA(certId,publickey);


    }

    @RequestMapping(value = "/device",method = RequestMethod.POST)
    @ApiOperation(value = "设备申请数字证书",notes = "设备申请数字证书,参数certid：产品序列信息，参数publickey：本地公钥",httpMethod = "POST")
    public BaseResult applyDeviceCA(@RequestParam(value = "certid") String certId, @RequestParam(value = "publickey") String publickey) throws Exception {

        return mqttDeviceService.applyDeviceCA(certId,publickey);


    }


}
