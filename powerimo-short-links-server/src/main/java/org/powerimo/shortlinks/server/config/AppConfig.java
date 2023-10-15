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

    @Value("${app.ttl:30}")
    private int defaultTtl = 30;

    @Value("${app.stat-interval:5}")
    private int statInterval;
}
