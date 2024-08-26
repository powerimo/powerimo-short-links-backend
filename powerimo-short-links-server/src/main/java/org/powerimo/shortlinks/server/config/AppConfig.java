package org.powerimo.shortlinks.server.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
@Slf4j
@Getter
public class AppConfig {

    @Value("${app.domain}")
    private String domain;

    @Value("${app.ttl:30}")
    private long defaultTtl = 30;

    @Value("${app.stat-interval:5}")
    private int statInterval;

    @Value("${app.cleanup:true}")
    private boolean cleanupEnabled;

    @Value("${app.only-hyperlinks:false}")
    private boolean onlyHyperlinks;

    @Value("${app.frontend.path:/app}")
    private String frontendPath;

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }

    public String getNotFoundPath() {
        return frontendPath + "/404";
    }
}
