package org.powerimo.shortlinks.server.generators;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidCodeGenerator implements CodeGenerator {

    @Override
    public String generate(String url) {
        return UUID.randomUUID().toString();
    }
}
