package com.changgou.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

//使用 component之后，整个项目都不用在new 了。
//当不加component类注解时，需要向在user微服务中在启动类中new TokenDecode一样new FeignInterceptor.
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //记录了当前用户请求的所有数据，包含请求头和请求参数等。
        //用户当前请求的时候对应线程的数据。  然后开启熔断之后，使用新的线程，就导致加载的数据不存在。
        //如果开启熔断，默认是线程池隔离，会开启新的线程，需要将熔断策略换成信号量隔离，此时不会开启新的线程。
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null){
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if (request != null){
                //获取请求头中的数据
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()){
                    //请求头的key
                    String headerName = headerNames.nextElement();
                    if ("authorization".equals(headerName)){  //这里只取 token ,其他的滤掉
                        //请求头的value
                        String headerValue = request.getHeader(headerName); // Bearer jwt
                        //传递令牌
                        requestTemplate.header(headerName,headerValue);
                    }
                }
            }
        }
    }
}

//package com.changgou.order.interceptor;
//
//
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.util.Enumeration;
//
///**
// * Author: QX_He
// * DATA: 2020/6/4-22:38
// * Description:
// **/
//@Configuration
//public class TokenRequestInterceptor implements RequestInterceptor {
//    /**
//     * 获取用户的令牌，将令牌在分装到头文件中。
//     * @param requestTemplate
//     */
//    @Override
//    public void apply(RequestTemplate requestTemplate) {
//        //记录了当前用户请求的所有数据，包含请求头和请求参数等。
//        //用户当前请求的时候对应线程的数据。  然后开启熔断之后，使用新的线程，就导致加载的数据不存在。
//        //如果开启熔断，默认是线程池隔离，会开启新的线程，需要将熔断策略换成信号量隔离，此时不会开启新的线程。
//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        //获取请求头中的数据
//        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
//        while (headerNames.hasMoreElements()){
//            //请求头的key
//            String headerKey = headerNames.nextElement();
//            //请求头的value
//            String headerValue = requestAttributes.getRequest().getHeader(headerKey);
//            //打印 调试
//            System.out.println(headerKey+":"+headerValue);
//            //将请求头信息封装到头中，使用Feign调用的时候，会传递给下一个微服务
//            requestTemplate.header(headerKey,headerValue);
//        }
//    }
//}
