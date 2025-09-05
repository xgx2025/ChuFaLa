package com.hope;

import cn.hutool.crypto.SecureUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;

@SpringBootTest
class ChuFaLaApplicationTests {

    @Test
    void getMD5Value() {
        System.out.println(SecureUtil.md5("123456"));
    }

}
