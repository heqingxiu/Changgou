package com.changgou;

import com.changgou.order.config.TokenDecode;
import com.changgou.user.feign.UserFeign;
import com.changgou.util.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.order.dao"})
@EnableFeignClients(basePackages = {"com.changgou"})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }

    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }

    /**
     * 用于生成主键
     *
     * @return
     */
    @Bean
    public IdWorker newIdWorker() {
        return new IdWorker();
    }
}
