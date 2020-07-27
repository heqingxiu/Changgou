package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WXPayService;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class WXPayServiceImpl implements WXPayService {

    @Autowired
    private WXPay wxPay;

    @Value("${weixin.notifyUrl}")
    private String notify_url;


    // Multi Micro service pay interface
    // Parameter of Map need below para:   exchange, routingKey,orderId,money
    @Override
    public Map createNative(Map<String, String> map) {
        try {
            // Multi Service pay process , add a dynamic exchange and routing setting.
            Map<String, String> prePayData = new HashMap<>(); // Primary Map
            Map<String, String> map1 = new HashMap<String, String>();// MQ map
            String Exchange = map.get("exchange");
            String Routing = map.get("routingKey");
            map1.put("exchange", Exchange);
            map1.put("routingKey", Routing);
            String attach = JSON.toJSONString(map1);
            System.out.println("Attach:" + attach);
            prePayData.put("attach", attach);

            // Basically setting
            prePayData.put("body", "畅购");
            prePayData.put("out_trade_no", map.get("orderId"));
            prePayData.put("detail", "购物买了一个杯子");
            prePayData.put("total_fee", map.get("money"));
            prePayData.put("spbill_create_ip", "127.0.0.1"); // 终端ip
            prePayData.put("notify_url", notify_url);
            prePayData.put("trade_type", "NATIVE");
            //2.基于wxpay完成统一下单接口的调用,并获取返回结果
            Map<String, String> result = wxPay.unifiedOrder(prePayData);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    //统一下单接口调用
    @Override
    public Map nativePay(String orderId, Integer money) {
        try {
            //1. 封装请求参数
            Map<String, String> map = new HashMap<>();
            map.put("body", "畅购");
            map.put("out_trade_no", orderId);
            map.put("detail", "购物买了一个杯子");

//            BigDecimal payMoney = new BigDecimal("0.01");
//            BigDecimal fen = payMoney.multiply(new BigDecimal("100")); //1.00
//            fen = fen.setScale(0,BigDecimal.ROUND_UP); // 1
//            map.put("total_fee",String.valueOf(fen));

            // Dynamic calculate the money
            map.put("total_fee", money.toString());
            map.put("spbill_create_ip", "127.0.0.1"); // 终端ip
            map.put("notify_url", notify_url);
            map.put("trade_type", "NATIVE");

            //2.基于wxpay完成统一下单接口的调用,并获取返回结果
            Map<String, String> result = wxPay.unifiedOrder(map);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map queryOrder(String orderId) {
        try {
            Map<String, String> map = new HashMap();
            map.put("out_trade_no", orderId);
            Map<String, String> resultMap = wxPay.orderQuery(map);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closeOrder(String orderId) {
        try {
            Map<String, String> map = new HashMap();
            map.put("out_trade_no", orderId);
            Map<String, String> resultMap = wxPay.closeOrder(map);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
