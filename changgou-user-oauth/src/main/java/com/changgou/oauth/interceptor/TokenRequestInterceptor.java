package com.changgou.oauth.interceptor;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * Author: QX_He
 * DATA: 2020/6/4-22:38
 * Description:
 **/
@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {
    //权限
    String[] Power ={"admin","oauth"};

    @Override
    public void apply(RequestTemplate requestTemplate) {

        //1.生成admin令牌
        String token = AdminToken.adminToken(Power);
        //2.将token放到header中并且 key 为 Authorization
        requestTemplate.header("Authorization","bearer "+token);
    }
}
