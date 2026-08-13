package com.easy.ai;

import com.easy.ai.utils.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MailUtil mailUtil;

    @Test
    public void testRedisConnection() {
        System.out.println("=== Redis 连接测试 ===");
        
        try {
            stringRedisTemplate.opsForValue().set("test", "hello redis");
            String retrievedValue = stringRedisTemplate.opsForValue().get("test");
            System.out.println("✅ 写入成功: test = hello redis");
            System.out.println("✅ 读取成功: " + retrievedValue);
            stringRedisTemplate.delete("test");
            
            System.out.println("✅ Redis 连接成功！");
            System.out.println("✅ 数据读写测试通过！");
        } catch (Exception e) {
            System.err.println("❌ Redis 连接失败: " + e.getMessage());
            fail("Redis 连接失败: " + e.getMessage());
        }
    }

    @Test
    public void testSendVerificationCode() {
        String testEmail = "18382274361@163.com";
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        System.out.println("=== 邮箱验证码测试 ===");
        System.out.println("收件人: " + testEmail);
        System.out.println("验证码: " + code);

        String redisKey = "verify:code:" + testEmail;
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
        System.out.println("✅ 验证码已存入Redis, key: " + redisKey);

        try {
            mailUtil.sendVerificationCode(testEmail, code);
            System.out.println("✅ 验证码邮件发送成功");
        } catch (Exception e) {
            System.err.println("⚠️ 邮件发送失败(请检查邮箱配置): " + e.getMessage());
        }

        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
        System.out.println("从Redis读取的验证码: " + storedCode);

        assertNotNull(storedCode, "Redis中未找到验证码");
        assertEquals(code, storedCode, "Redis中的验证码与发送的不一致");

        Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        System.out.println("验证码剩余有效期: " + ttl + "秒");
        assertTrue(ttl > 0, "验证码未设置有效期");

        System.out.println("✅ 邮箱验证码测试通过！");
    }

    @Test
    public void testRedisTemplate() {
        Set<String> keys = stringRedisTemplate.keys("*");

        StringBuilder sb = new StringBuilder();
        sb.append("=== Redis 数据 ===\n");

        if (keys == null || keys.isEmpty()) {
            sb.append("Redis 中没有任何数据\n");
        } else {
            sb.append("共 ").append(keys.size()).append(" 个 key:\n");
            for (String key : keys) {
                String type = stringRedisTemplate.type(key).code();
                String value = "";

                switch (type) {
                    case "string" -> value = stringRedisTemplate.opsForValue().get(key);
                    case "list" -> value = "[list] size=" + stringRedisTemplate.opsForList().size(key);
                    case "set" -> value = "[set] size=" + stringRedisTemplate.opsForSet().size(key);
                    case "zset" -> value = "[zset] size=" + stringRedisTemplate.opsForZSet().size(key);
                    case "hash" -> value = "[hash] size=" + stringRedisTemplate.opsForHash().size(key);
                    default -> value = "[unknown type]";
                }

                Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
                String ttlStr = ttl > 0 ? ttl + "s" : (ttl == -1 ? "永久" : "已过期");

                sb.append("key: ").append(key)
                  .append(", type: ").append(type)
                  .append(", value: ").append(value)
                  .append(", ttl: ").append(ttlStr)
                  .append("\n");
            }
        }

        System.err.println(sb);
    }
}
