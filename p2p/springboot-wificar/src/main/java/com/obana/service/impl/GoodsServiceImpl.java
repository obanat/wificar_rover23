package com.obana.service.impl;

import com.alibaba.fastjson.JSONObject;

import com.obana.model.carInfo;
import com.obana.service.GoodsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author obana
 * @version 1.0
 * @date 2022/10/26 13:55
 * @description
 * @modify
 */
@Service(value = "GoodsService")
public class GoodsServiceImpl implements GoodsService {

    private List<carInfo> infoList= new ArrayList<carInfo>();
    @Override
    public JSONObject regClient(String mac, String time, String ipaddr){
        JSONObject result = new JSONObject();
        if (mac == null || mac.length() <= 2) {
            result.put("code", "500");
            result.put("msg", "注册异常！");
            return result;
        }

        for (int i = 0; i<infoList.size();i++) {
            carInfo info = infoList.get(i);
            if (info != null && mac.equals(info.getMac())) {
                info.setTime(time);
                info.setIpaddr(ipaddr);
                result.put("code", "0");
                result.put("msg", "注册成功！");
                return result;
            }
        }
        //add a new client
        carInfo info = new carInfo();
        info.setMac(mac);
        info.setTime(time);
        info.setIpaddr(ipaddr);
        infoList.add(info);
        result.put("code", "0");
        result.put("msg", "注册成功！");
        return result;

    }

    @Override
    public JSONObject getClientIp(String mac){
        //String result = "error";
        JSONObject result = new JSONObject();
        if (mac == null || mac.length() == 0) {
            mac = "112233";//this is default mac
        }

        for (int i = 0; i<infoList.size();i++) {
            carInfo info = infoList.get(i);
            if (info != null && mac.equals(info.getMac())) {
                result.put("code", "0");
                result.put("msg", "注册成功！");
                result.put("ipaddr", info.getIpaddr());
                result.put("time", info.getTime());
                return result ;
            }
        }
        result.put("code", "500");
        result.put("msg", "request error");
        return result;
    }
}
