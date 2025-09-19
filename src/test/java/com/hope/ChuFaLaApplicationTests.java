package com.hope;

import cn.hutool.crypto.SecureUtil;
import com.hope.utils.RedisWorker;
import com.hope.utils.SignaturePriceUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@SpringBootTest
class ChuFaLaApplicationTests {
    @Autowired
    private RedisWorker redisWorker;

    @Test
    void getMD5Value() {
        System.out.println(SecureUtil.md5("123456"));
    }

    @Test
    void generateId(){
        System.out.println(redisWorker.nextId("HOTEL"));
    }

    @Test
    void signaturePrice(){
        // 1. 生成价格和签名
        Map<String, String> priceData = SignaturePriceUtil.generatePriceWithSignature(
                "ORD123456", "2023-10-01","2023-10-04" ,3,2,99.99);
        System.out.println("生成的数据: " + priceData.get("data"));
        System.out.println("生成的签名: " + priceData.get("signature"));

        // 2. 模拟前端传递数据，验证签名
        boolean isValid = SignaturePriceUtil.verifyPriceSignature(
                priceData.get("data"), priceData.get("signature"));
        System.out.println("签名验证结果: " + (isValid ? "通过" : "失败"));
    }


    @Test
    void LocalDataTest (){
        LocalDate checkIn =LocalDate.parse("2023-10-01");
        LocalDate checkOut =LocalDate.parse("2023-10-04");
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        System.out.println(nights);
    }

    @Test
    void signaturePriceValidTest(){
        String data = "roomId=95710232340070401&checkIn=2025-09-16&checkOut=2025-09-18&nightNum=2,totalPrice=1880.00&timestamp=1758032268278";
        String signature = "HEBm4B7xIea4QXgoJVeVOLm7XToQfKjJqmdImHojxuk=";
        boolean isValid = SignaturePriceUtil.verifyPriceSignature(data, signature);
        System.out.println(isValid);
    }

}
