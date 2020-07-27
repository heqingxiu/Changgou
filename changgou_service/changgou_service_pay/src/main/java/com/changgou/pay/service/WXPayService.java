package com.changgou.pay.service;

import java.util.Map;

public interface WXPayService {


    Map createNative(Map<String, String> map);

    //扫码支付
    Map nativePay(String orderId, Integer money);

    //基于微信查询订单
    Map queryOrder(String orderId);

    //基于微信关闭订单
    Map closeOrder(String orderId);
}
