package com.easy.ai.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("USED CAT 邮箱验证码");

            String html = "<div style='padding:20px;background:#fafafa;'>"
                    + "<h2 style='color:#1a1a1a;'>USED CAT 二手猫</h2>"
                    + "<p style='font-size:16px;'>您的验证码是：</p>"
                    + "<div style='font-size:28px;font-weight:bold;color:#1a1a1a;"
                    + "background:#f0f0f0;display:inline-block;padding:10px 30px;"
                    + "border-radius:8px;letter-spacing:4px;'>" + code + "</div>"
                    + "<p style='margin-top:20px;color:#999;'>验证码5分钟内有效，请勿泄露他人。</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("验证码已发送至 {}", to);
        } catch (MessagingException e) {
            log.error("发送验证码失败", e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }
}
