package com.changgou.seckill.service.impl;

import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecKillGoodsServiceImpl implements SecKillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SECKILL_GOODS_KEY = "seckill_goods_";

    public static final String SECKILL_GOODS_STOCK_COUNT_KEY = "seckill_goods_stock_count_";


    /**
     * According to time-period and commodity id search the detail of goods.
     * mainly parameter: time-period , commodity id
     */
    @Override
    public SeckillGoods one(String time, long id) {
        return (SeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).get(id);
    }

    /**
     * Query all goods messages.
     * The difference of between "seckill_goods_" and "seckillgoods_stock_count_" is as follows:
     * 1. redisTemplate.boundHashOps() include three parameters ,respectively : maneSpace , hashKey ,hasValue
     * 2. redisTemplate.opsForValue() only include two parameters ,respectively: maneSpace, value
     */
    @Override
    public List<SeckillGoods> list(String time) {
        //Get all goods from redis cache via hashBounds methods
        List<SeckillGoods> list = redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).values();

        //更新库存数据的来源      What is the function of this function?  Update the stock of goods again
        for (SeckillGoods seckillGoods : list) {
            String value = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY + seckillGoods.getId());
            seckillGoods.setStockCount(Integer.parseInt(value)); // Get data from redis stock.
        }
        return list;
    }


}
