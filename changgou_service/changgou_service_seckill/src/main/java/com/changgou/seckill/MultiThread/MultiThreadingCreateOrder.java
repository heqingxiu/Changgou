package com.changgou.seckill.MultiThread;

import com.changgou.entity.SecKillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Author: QX_He
 * DATA: 2020/7/12-22:02
 * Description:
 **/
@Component
public class MultiThreadingCreateOrder {

    public static final String SECKILL_OREDER_QUEUE = "SeckillOrderQueue";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * MultiThread hijack function
     */
    @Async
    public void hijackOrderFromRedisList() {
        try {
            // Test step
            System.out.println("Thread sleep");
            Thread.sleep(10000); // 10 s
            System.out.println("Wake up after 10s ");
            /*****/

            Object object = redisTemplate.boundListOps(SECKILL_OREDER_QUEUE).rightPop();
            SecKillStatus secKillStatus = (SecKillStatus) object;

            // if it is empty, then return .
            if (secKillStatus == null) {
                return;
            }
            //  Parse the retrieved data
            String name = secKillStatus.getUserName();
            Long id = secKillStatus.getGoodsId();
            String time = secKillStatus.getTime();

            System.out.println("Order success");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
