package org.powerimo.shortlinks.server.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Getter
public class AppConfig {

    @Value("${app.domain}")
    private String domain;

    private int defaultTtl = 300;
}
