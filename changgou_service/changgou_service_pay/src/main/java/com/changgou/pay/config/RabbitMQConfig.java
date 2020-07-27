package com.changgou.pay.config;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


/**
 * 用于自动创建队列。 实际开发中是区rabbit mq的网页中 手动创建 MQ
 */

@Configuration
public class RabbitMQConfig {

    // use:  Can use "String" format to get parameters from application.yml
    //   "String " <=> ${"String"}
    @Autowired
    private Environment env;

    /*********************Basically pay queue************************/
    // Create Queue
    @Bean
    public Queue basicallyQueue() {
        return new Queue(env.getProperty("mq.pay.queue.basically"));
    }

    // Create exchange
    @Bean
    public Exchange basicallyExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.basically"));
    }

    // Binding exchange
    @Bean
    public Binding basicallyBinding(Queue basicallyQueue, Exchange basicallyExchange) {
        return BindingBuilder.bind(basicallyQueue).to(basicallyExchange).with(env.getProperty("mq.pay.routingKey.basically")).noargs();

    }

    /*********************SecKill pay queue************************/

    // Create Queue
    @Bean
    public Queue secKillQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckill"));
    }

    // Create exchange
    @Bean
    public Exchange secKillExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckill"));
    }

    // Binding exchange
    @Bean
    public Binding secKillBinding(Queue secKillQueue, Exchange secKillExchange) {
        return BindingBuilder.bind(secKillQueue).to(secKillExchange).with(env.getProperty("mq.pay.routingKey.seckill")).noargs();

    }
}
