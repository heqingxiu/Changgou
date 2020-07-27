package com.changgou.seckill.mq;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Author: QX_He
 * DATA: 2020/7/14-21:57
 * Description:
 **/
@RabbitListener(queues = "${mq.pay.queue.seckill}")
@Component
public class SecKillPayListener {

    @RabbitHandler
    public void SecKillPayHandler(String messages) {

        //  String type is turned to Map type by JSON tool
        Map<String, String> getMessages = JSON.parseObject(messages, Map.class);
        System.out.println("Messages:" + getMessages);

        /**
         *  Add order number and update stock.
         */


    }

}
