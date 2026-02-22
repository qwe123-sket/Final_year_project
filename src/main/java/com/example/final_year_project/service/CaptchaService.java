package com.example.final_year_project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "captcha:";
    private static final int EXPIRE_MINUTES = 5;
    private static final int WIDTH = 150;
    private static final int HEIGHT = 48;
    private static final int CODE_LEN = 4;

    // key + base64 png image
    public Map<String, String> generate() {
        String code = randomCode();
        String key = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(PREFIX + key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);

        String base64 = drawImage(code);
        return Map.of("key", key, "image", "data:image/png;base64," + base64);
    }

    // one-time use, delete after verify
    public boolean verify(String key, String inputCode) {
        if (key == null || inputCode == null) return false;
        String stored = redisTemplate.opsForValue().get(PREFIX + key);
        redisTemplate.delete(PREFIX + key);
        return stored != null && stored.equalsIgnoreCase(inputCode.trim());
    }

    private String randomCode() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(CODE_LEN);
        for (int i = 0; i < CODE_LEN; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    private String drawImage(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Random rnd = new Random();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 245, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(180 + rnd.nextInt(60), 180 + rnd.nextInt(60), 200 + rnd.nextInt(50)));
            g.drawLine(rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT), rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT));
        }

        // 干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(160 + rnd.nextInt(80), 160 + rnd.nextInt(80), 180 + rnd.nextInt(60)));
            g.fillOval(rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT), 2, 2);
        }

        // 绘制验证码字符
        String[] fonts = {"Arial", "Verdana", "Tahoma", "Georgia"};
        for (int i = 0; i < code.length(); i++) {
            g.setFont(new Font(fonts[rnd.nextInt(fonts.length)], Font.BOLD, 28 + rnd.nextInt(6)));
            g.setColor(new Color(40 + rnd.nextInt(80), 40 + rnd.nextInt(80), 80 + rnd.nextInt(80)));
            double angle = (rnd.nextDouble() - 0.5) * 0.4;
            int x = 18 + i * 32;
            int y = 34 + rnd.nextInt(6);
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }

        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate captcha image", e);
        }
    }
}
