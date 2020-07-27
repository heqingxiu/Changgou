package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "goods")
public interface SkuFeign {

    @GetMapping("/sku/spu/{spuId}")
    public List<Sku> findSkuListBySpuId(@PathVariable("spuId") String spuId);

    @GetMapping("/sku/{id}")
    public Result<Sku> findById(@PathVariable("id") String id);

//    @PostMapping("/sku/decr/count")
//    public Result decrCount(@RequestParam("username") String username);
    /**
     *  减少库存 增加 销售数量
     * @param decrMap
     * @return
     */
    @GetMapping(value = "/sku/decr/count")
    public Result decrCount(@RequestParam Map<String,Integer> decrMap);

    /**
     * 回滚库存
     * @param skuId
     * @param num
     * @return
     */
    @RequestMapping("/sku/resumeStockNum")
    public Result resumeStockNum(@RequestParam("skuId") String skuId,@RequestParam("num")Integer num);
}
