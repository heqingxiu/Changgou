package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.SecKillStatus;
import com.changgou.seckill.MultiThread.MultiThreadingCreateOrder;
import com.changgou.seckill.config.ConfirmMessageSender;
import com.changgou.seckill.config.RabbitMQConfig;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SecKillOrderService;
import com.changgou.util.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SECKILL_GOODS_KEY = "seckill_goods_";

    public static final String SECKILL_GOODS_STOCK_COUNT_KEY = "seckill_goods_stock_count_";

    public static final String SECKILL_OREDER_QUEUE = "SeckillOrderQueue";

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ConfirmMessageSender confirmMessageSender;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Override
    public boolean add(Long id, String time, String username) {
        /**
         * 1.获取redis中的商品信息与库存信息,并进行判断
         * 2.执行redis的预扣减库存操作,并获取扣减之后的库存值
         * 3.如果扣减之后的库存值<=0,则删除redis中响应的商品信息与库存信息
         * 4.基于mq完成mysql的数据同步,进行异步下单并扣减库存(mysql)
         * 5.Concurrent issues:  Only occur in when the goods is added by user.
         *   So, we need to avoid to write and read sql database directly.
         *   Solution: Push the order to redis when that coming , Will be processed later.
         */
        //防止用户恶意刷单（5分钟超时时间，可以再抢）   Use redis`s self-increment methods to prevent the user always to order.
        String result = this.preventRepeatCommit(username, id);
        if ("fail".equals(result)) {
            return false;
        }

        //防止相同商品重复购买  ，如果已经有订单了，就不给再买了（因为有五分钟的超时时间，故需要这个判断）
        SeckillOrder order = seckillOrderMapper.getOrderInfoByUserNameAndGoodsId(username, id);
        if (order != null) {
            return false;
        }
        /**
         *  Over-sell
         *  Solution:   Use the redis`s list queue to resolve.
         *  当查询的list 队列中的数量大于0 证明抢单成功。
         *  为了减少Mysql的压力，我们使用redis list queue的机制来防止超卖现象。
         */
        //获取商品信息
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).get(id);
        //获取库存信息,
        String redisStock = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        if (StringUtils.isEmpty(redisStock)) {
            return false;
        }
        int stock = Integer.parseInt(redisStock);
        if (seckillGoods == null || stock <= 0) {
            return false;
        }

        //执行redis的预扣减库存,并获取到扣减之后的库存值
        //decrement:减 increment:加     ->    Lua脚本语言
        Long decrement = redisTemplate.opsForValue().decrement(SECKILL_GOODS_STOCK_COUNT_KEY + id);  // The stock will be decremented when this function was called.
        if (decrement <= 0) {
            //扣减完库存之后,当前商品已经没有库存了.
            //删除redis中的商品信息与库存信息
            redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).delete(id);
            redisTemplate.delete(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        }

        //发送消息(保证消息生产者对于消息的不丢失实现)
        //消息体: 秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(String.valueOf(seckillGoods.getCostPrice()));
        seckillOrder.setUserId(username);
//        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");

        //发送消息 , It is use to decrement stock . the listener in consume service.
        confirmMessageSender.sendMessage("", RabbitMQConfig.SECKILL_ORDER_QUEUE, JSON.toJSONString(seckillOrder));

        // 下单成功的，30 分钟支付超时查询
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HHJ:mm:ss");
        System.out.println("seckill 下单时间" + simpleDateFormat.format(new Date()));
        rabbitTemplate.convertAndSend("secKillDelayQueue", (Object) seckillOrder.getUserId(), new MessagePostProcessor() {
            @Override                                                //  ↑ ↑    这里要用Object 转一下，不然会报错
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("10000"); // Current : 10s
                return message;
            }
        });


        return true;
    }

    private String preventRepeatCommit(String username, Long id) {
        String redis_key = "seckill_user_" + username + "_id_" + id;

        // The value of  variable "redis_key" will keep and add 1 base on the first init during 5 min .
        long count = redisTemplate.opsForValue().increment(redis_key, 1);
        if (count == 1) {
            //代表当前用户是第一次访问.
            //对当前的key设置一个五分钟的有效期
            redisTemplate.expire(redis_key, 5, TimeUnit.MINUTES);
            return "success";
        }

        if (count > 1) {
            return "fail";
        }

        return "fail";
    }
}
