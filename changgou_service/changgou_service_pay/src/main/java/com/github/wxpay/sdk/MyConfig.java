package com.github.wxpay.sdk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.InputStream;

/**
 * User  self-definition
 */

public class MyConfig extends WXPayConfig {

    //Get data from <application.properties> file
//    /**
//     * 读取配置文件中的信息的 对象
//     */
//    @Autowired  //import org.springframework.core.env.Environment;
//    private Environment evn;
//
//    @Override
//    String getAppID() {return evn.getProperty("weixin.appid");}
//
//    @Override
//    String getMchID() {return evn.getProperty("weixin.partner");}
//
//    @Override
//    String getKey() { return evn.getProperty("weixin.partnerkey");}

    @Override
    String getAppID() {return "wx8397f8696b538317";}

    @Override
    String getMchID() {return "1473426802";}

    @Override
    String getKey() { return "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb";}

    @Override
    InputStream getCertStream() {
        return null;
    }

    @Override
    IWXPayDomain getWXPayDomain() {
        return new IWXPayDomain() {
            @Override
            public void report(String s, long l, Exception e) {
            }

            @Override
            public DomainInfo getDomain(WXPayConfig wxPayConfig) {
                return new DomainInfo("api.mch.weixin.qq.com", true);
            }
        };
    }
}
