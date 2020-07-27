package com.changgou.oauth.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: QX_He
 * DATA: 2020/6/4-22:36
 * Description:
 **/
public class AdminToken {

    public static String adminToken(String[] arg) {

        //加载证书，读取类路径中的文件
        ClassPathResource resource = new ClassPathResource("changgou.jks");
        //读取证书数据，加载读取证书数据
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, "changgou".toCharArray());
        //获取证书中的一对密钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou", "changgou".toCharArray());
        //获取私钥->RSA算法
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //创建令牌，需要私钥加盐【RAS算法】
        Map<String, Object> payload = new HashMap<>();
     //   payload.put("authorities",new String[]{"admin","oauth"}); //  加载额外信息
        payload.put("authorities", arg); //  加载额外信息,这里不是 Authorization

        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        //获取令牌数据
        String token = jwt.getEncoded();
        return token;
    }
}
