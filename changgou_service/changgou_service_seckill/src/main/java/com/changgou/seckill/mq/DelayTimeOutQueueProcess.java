package com.changgou.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.SecKillStatus;
import com.changgou.seckill.service.SecKillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Author: QX_He
 * DATA: 2020/7/15-22:21
 * Description:
 **/
@Component
@RabbitListener(queues = "secKillListenerQueue")
public class DelayTimeOutQueueProcess {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    SecKillOrderService secKillOrderService;

    @RabbitHandler
    public void getMessages(String messages) {

        try {
            // 获取用户信息
            SecKillStatus secKillStatus = JSON.parseObject(messages, SecKillStatus.class);

            //如果此时Redis中没有用户排队信息，则表明订单已经处理，如果有排队信息，则表示在超时的这段时间内未支付成功，那么关闭订单，删除排队信息
            Object userQueueStatus = redisTemplate.boundHashOps("UserQueueStatus").get(secKillStatus.getUserName());
            if (userQueueStatus != null) {
                //关闭微信支付

                //删除订单
//                secKillOrderService.deleteOrder(secKillStatus.getUserName());

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
