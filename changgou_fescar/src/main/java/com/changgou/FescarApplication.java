package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Author: QX_He
 * DATA: 2020/7/19-20:00
 * Description:
 **/
@SpringBootApplication
@EnableEurekaClient
public class FescarApplication {

    public static void main(String[] args) {
        SpringApplication.run(FescarApplication.class);

    }
}
