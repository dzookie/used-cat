package com.easy.ai;

import org.junit.jupiter.api.Test;

import com.easy.ai.utils.PasswordUtil;

public class PwdTest {
    @Test
    public void testPasswordEncoding() {
        String password = "admin123";
        String encodedPassword = PasswordUtil.encode(password);
        System.out.println(encodedPassword);
    }
}
