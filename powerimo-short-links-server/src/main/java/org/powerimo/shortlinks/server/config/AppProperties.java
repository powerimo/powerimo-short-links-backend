package org.powerimo.shortlinks.server.config;

import lombok.Getter;
import lombok.Setter;
import org.powerimo.shortlinks.server.generators.StringCodeGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private String domain;
    private Long ttl = 60 * 60 * 24 * 365L;
    private String generatorClass = StringCodeGenerator.class.getCanonicalName();
    private boolean cleanup = true;
    private boolean onlyHyperlinks = false;
    private int statInterval = 5;
    private String frontendContextPath = "/app";
}
