package com.obana.controller;

import com.alibaba.fastjson.JSONObject;
import com.obana.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author obana
 * @version 1.0
 * @date 2023/03/21 12:41
 * @description
 * @modify
 */
@RestController
@RequestMapping(value = "/wificar")
public class redisController {
    @Autowired
    private GoodsService goodsService;


    @RequestMapping(value = "/regClient", method = RequestMethod.POST)
    public JSONObject regClient(@RequestParam("mac") String mac, @RequestParam("time") String time, @RequestParam("ipaddr") String ipaddr) {
        return goodsService.regClient(mac, time, ipaddr);
    }

    @RequestMapping(value = "/getClientIp", method = RequestMethod.GET)
    public String getClientIp() {
        String mac = "";
        return goodsService.getClientIp(mac);
    }
}
