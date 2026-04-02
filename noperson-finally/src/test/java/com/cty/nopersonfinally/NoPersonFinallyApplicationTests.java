package com.cty.nopersonfinally;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class NoPersonFinallyApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    void contextLoads() {
        Long add = stringRedisTemplate.opsForSet().add("flyer:idle:devices:1", "1");
        System.out.println(add);
    }

}
