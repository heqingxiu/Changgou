import com.alibaba.fastjson.JSON;
import com.changgou.entity.HttpClient;
import com.changgou.pay.config.RabbitMQConfig;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Author: QX_He
 * DATA: 2020/6/25-11:35
 * Description: HttpClient工具类测试案例
 **/
public class HttpClientTest {

    /**
     * HttpClient primary function: Sent http/https request.
     */
    @Test
    public void HttpClientTest() throws IOException {
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        //Create HttpClients Object
        HttpClient httpClient = new HttpClient(url);
        //Date will be sent
        String Xml = "<xml><name>qingxiu</name></xml>";
        //Set the parameter of XMl which will be request
        httpClient.setXmlParam(Xml);
        //Choose https/http
        httpClient.setHttps(true);
        //Send request with POST
        httpClient.post();
        //Get the date of response
        String result = httpClient.getContent();
        System.out.println("result:" + result);
    }

}
