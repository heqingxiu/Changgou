package com.changgou.seckill.mq.QueueTimeOutConfig;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author: QX_He
 * DATA: 2020/7/15-21:49
 * Description:
 **/
@Configuration
public class TimeOutQueueConfig {

    /**
     * 创建 MQ 1
     */
    @Bean
    public Queue secKillDelayQueue() {
        return QueueBuilder
                .durable("secKillDelayQueue")
                //param 1 : 死信队列  ；  Param 2 ： 被MQ2绑定的交换机
                .withArgument("x-dead-letter-exchange", "secKillListenerExchange") //orderDelayQueue队列信息会过期，过期子后，进入到死信队列，死信队列数据绑定到其他交换机
                .withArgument("x-dead-letter-routing-key", "secKillListenerQueue") // 设置路由到哪一个队列
                .build();
    }

    /**
     * 创建 MQ2
     */
    @Bean
    public Queue secKillListenerQueue() {
        return new Queue("secKillListenerQueue", true);
    }

    /**
     * 创建交换机
     */
    @Bean
    public Exchange secKillListenerExchange() {
        return new DirectExchange("secKillListenerExchange");
    }

    /**
     * MQ2 绑定交换机
     */
    @Bean
    public Binding secKillListenerBinding(Queue secKillListenerQueue, Exchange secKillListenerExchange) {
        return BindingBuilder.bind(secKillListenerQueue).to(secKillListenerExchange).with("secKillListenerQueue").noargs();
    }
}
