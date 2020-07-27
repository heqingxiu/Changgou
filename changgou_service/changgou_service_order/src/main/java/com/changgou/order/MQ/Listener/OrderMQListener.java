package com.changgou.order.MQ.Listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.impl.OrderServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Author: QX_He
 * DATA: 2020/6/28-14:44
 * Description:
 **/
@RabbitListener(queues = "${mq.pay.queue.basically}")
@Component
public class OrderMQListener {

    @Autowired
    private OrderServiceImpl orderService;


    /**
     * 支付结果监听   非延时MQ队列（非用于 30 min 延迟取消订单队列）
     */
    @RabbitHandler
    public void getMessages(String message) throws Exception {
        //支付结果获取
        Map<String, String> maps = JSON.parseObject(message, Map.class);
        System.out.println("监听到的消息:" + maps);

        //微信支付流水号  transaction_id
        String transaction = maps.get("transaction_id");
        System.out.println("交易流水号为：" + transaction);
        //订单号
        String orderId = maps.get("out_trade_no");
        System.out.println("订单号：" + orderId);

        if ("SUCCESS".equals(maps.get("return_code"))) {

            if ("SUCCESS".equals(maps.get("result_code"))) {
                //更新数据
                orderService.updateStatus(maps.get("out_trade_no"), maps.get("time_end"), maps.get("transaction_id"));
            } else {
                //支付失败，关闭支付，取消订单，回滚数据。
                orderService.deleteOrder(maps.get("out_trade_no"));
            }
        }


        // 通信标识 return_code
//        String return_code  =  maps.get("return_code");
//        if(return_code.equals("SUCCESS")){
//            //业务结果
//            String result_code = maps.get("result_code");
//            if(result_code.equals("SUCCESS")){
//                //微信支付流水号  transaction_id
//                String transaction = maps.get("transaction_id");
//                System.out.println("交易流水号为："+transaction);
//                //操作数据库，修改支付结果为 true.
//
//            }else{
//                    //支付失败，关闭支付，取消订单，回滚数据。
//
//            }
//        }
    }
}
