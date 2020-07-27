package com.changgou.seckill.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.service.SecKillGoodsService;
import com.changgou.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/seckillgoods")
public class SecKillGoodsController {

    @Autowired
    private SecKillGoodsService secKillGoodsService;

    @RequestMapping("/list")
    public Result<List<SeckillGoods>> list(@RequestParam("time") String time) {
        List<SeckillGoods> seckillGoodsList = secKillGoodsService.list(time);
        return new Result<>(true, StatusCode.OK, "查询成功", seckillGoodsList);
    }

    /**
     * search seckill time menus   查询秒杀时间菜单，用于在前端显示当前的秒杀时间段
     */
    @GetMapping("/meus")
    public Result<List<Date>> menus() {
        List<Date> dataMenus = DateUtil.getDateMenus();
        return new Result<List<Date>>(true, StatusCode.OK, "timer query successfully", dataMenus);
    }

    /**
     * Query detail of seckill goods
     */
    @GetMapping("/one")
    public Result<SeckillGoods> one(String time, long id) {
        SeckillGoods seckillGoods = secKillGoodsService.one(time, id);
        return new Result<SeckillGoods>(true, StatusCode.OK, "Query detail of Seckill goods is success", seckillGoods);
    }

}
