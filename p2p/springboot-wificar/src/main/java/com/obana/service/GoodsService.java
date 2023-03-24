package com.obana.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;


@Service
public interface GoodsService {

    JSONObject regClient(String mac, String time, String ipaddr);

    String getClientIp(String mac);
}
