package com.cty.nopersonfinally.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {
    private StringRedisTemplate stringRedisTemplate;
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    private static final int COUNT_BITS = 32;
  public long nextId(String keyPrefix){
      //生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
      //生成序列号
      String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
      //生成一个key，获取当前日期，生成一天的key，id
      long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
      //生成
//      return 0L;
        return timestamp << COUNT_BITS | count;
  }

}
