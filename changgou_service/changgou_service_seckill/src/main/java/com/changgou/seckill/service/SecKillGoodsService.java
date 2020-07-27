package com.changgou.seckill.service;

import com.changgou.seckill.pojo.SeckillGoods;

import java.util.List;

public interface SecKillGoodsService {

    /**
     * search the detail of goods via commodity id and time-period
     *
     * @param commodity id , time-period
     */
    SeckillGoods one(String time, long id);


    List<SeckillGoods> list(String time);
}
