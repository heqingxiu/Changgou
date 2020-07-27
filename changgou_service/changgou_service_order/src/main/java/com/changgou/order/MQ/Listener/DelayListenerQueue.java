package com.changgou.order.MQ.Listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: QX_He
 * DATA: 2020/6/30-23:10
 * Description:
 **/

@RabbitListener(queues = "orderListenerQueue")
@Component
public class DelayListenerQueue {

    @RabbitHandler
    public void getDelayMessage(String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("监听到消息的时间:" + simpleDateFormat.format(new Date()));
        System.out.println("监听到消息内容:" + message);

        /**
         * 如果此时redis中没有用户排队信息，则表明该订单已经处理，如果有排队信息，则表明用户尚未完成支付，那么关闭订单【关闭微信支付】
         */
    }
}
