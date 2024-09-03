package org.powerimo.shortlinks.server.generators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@Slf4j
public class StringCodeGenerator implements CodeGenerator{
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Override
    public String generate(String url) {
        SecureRandom random = new SecureRandom();
        int length = random.nextInt(8) + 4; // Длина строки от 4 до 12
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        log.debug("For the URL ({}) the code has been generated: {}", url, sb);
        return sb.toString();
    }
}
