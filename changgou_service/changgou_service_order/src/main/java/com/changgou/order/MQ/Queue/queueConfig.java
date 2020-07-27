package com.changgou.order.MQ.Queue;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Author: QX_He
 * DATA: 2020/6/30-22:36
 * Description:   Delay Queue for basically pay service
 *
 **/
@Configuration
public class queueConfig {
    /**
     * 创建 MQ 1
     */
    @Bean
    public Queue orderDelayQueue() {
        return  QueueBuilder
                .durable("orderDelayQueue")
                //param 1 : 死信队列  ；  Param 2 ： 被MQ2绑定的交换机
                .withArgument("x-dead-letter-exchange", "orderListenerExchange") //orderDelayQueue队列信息会过期，过期子后，进入到死信队列，死信队列数据绑定到其他交换机
                .withArgument("x-dead-letter-routing-key", "orderListenerQueue") // 设置路由到哪一个队列
                .build();
    }

    /**
     * 创建 MQ2
     */
    @Bean
    public Queue orderListenerQueue() {
        return new Queue("orderListenerQueue", true);
    }

    /**
     * 创建交换机
     */
    @Bean
    public Exchange orderListenerExchange() {
        return new DirectExchange("orderListenerExchange");
    }

    /**
     * MQ2 绑定交换机
     */
    @Bean
    public Binding orderListenerBinding(Queue orderListenerQueue, Exchange orderListenerExchange) {
        return BindingBuilder.bind(orderListenerQueue).to(orderListenerExchange).with("orderListenerQueue").noargs();
    }
}
































