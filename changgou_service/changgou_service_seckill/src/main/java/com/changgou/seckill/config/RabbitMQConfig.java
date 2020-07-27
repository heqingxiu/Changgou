package com.changgou.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 秒杀商品订单信息
    public static final String SECKILL_ORDER_QUEUE="seckill_order";

    /**
     * Create persistence queue
     * @return
     */
    @Bean
    public Queue queue(){
        //  开启队列持久化
        return new Queue(SECKILL_ORDER_QUEUE,true);
    }
}
