import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: QX_He
 * DATA: 2020/6/25-10:55
 * Description:
 **/
public class WXUtilTest {

    @Test
    public void generateChar() throws Exception {
        /**
         * 生成随机字符
         */
        String str = WXPayUtil.generateNonceStr();
        System.out.println("随机字符串" + str);

        /**
         * 将 MAP 转换成XML 字符串
         */
        Map<String, String> maps = new HashMap<String, String>();
        maps.put("id", "1");
        maps.put("name", "zhangsan");
        maps.put("age", "18");
        String XML = WXPayUtil.mapToXml(maps);
        System.out.println("XML: " + XML);
        /**
         * 将MAP 转换成 XML ，并且生成签名
         */
        String signatureXml = WXPayUtil.generateSignedXml(maps, "heqingxiu");
        System.out.println("With signature：" + signatureXml);
        /**
         * 将XML 转换成 map
         */
        Map<String, String> toMap = new HashMap<String, String>();
        toMap = WXPayUtil.xmlToMap(signatureXml);
        System.out.println("toMap: " + toMap);

    }

    /**
     * 测试 从 application.properties中获取参数
     */
    @Value("${weixin.notifyUrl}")
    private String AppId;
    @Value("${weixin.partner}")
    private String MchID;
    @Value("${weixin.partnerkey}")
    private String  key;
    @Value("${weixin.notifyUrl}")
    private String notify_url;

    @Test
    public void getParameterFromApplication() {
        System.out.println("appid:" +AppId);
        System.out.println("MchId:"+MchID);
        System.out.println("key:"+key);
        System.out.println("notify"+notify_url);
    }
}
