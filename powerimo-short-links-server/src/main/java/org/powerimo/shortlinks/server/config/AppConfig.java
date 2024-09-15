package org.powerimo.shortlinks.server.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.powerimo.shortlinks.server.exceptions.InvalidArgument;
import org.powerimo.shortlinks.server.exceptions.InvalidConfigProperty;
import org.powerimo.shortlinks.server.generators.CodeGenerator;
import org.powerimo.shortlinks.server.generators.StringCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.Collection;

@Configuration
@Slf4j
@Getter
@RequiredArgsConstructor
public class AppConfig {
    private final AppProperties appProperties;
    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        log.info("Domain: {}", appProperties.getDomain());
        log.info("Frontend context path: {}", appProperties.getFrontendContextPath());
        log.info("Default TTL: {}", appProperties.getTtl());
    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }

    public String getNotFoundPath() {
        return appProperties.getFrontendContextPath() + "/404";
    }

    /**
     * Creates CodeGenerator instance based on application properties.
     * @return CodeGenerator implementation
     */
    @Bean
    public CodeGenerator codeGenerator() throws InvalidConfigProperty {
        Class<?> beanClass;
        try {
             beanClass = Class.forName(appProperties.getGeneratorClass());
        } catch (ClassNotFoundException ex) {
            throw new InvalidConfigProperty("Generator class not found: " + appProperties.getGeneratorClass()
                    + ". Please specify it by setting property 'app.generator-class'. Default value: " + StringCodeGenerator.class.getCanonicalName());
        }

        Object generatorBean;
        try {
            generatorBean = applicationContext.getBean(beanClass);
        } catch (Exception ex) {
            throw new InvalidConfigProperty("Bean of the generator class is not found. Please annotate it with '@Component' annotation. Generator class: " + beanClass.getCanonicalName());
        }

        if (generatorBean instanceof CodeGenerator) {
            log.info("Code generator class: {}", generatorBean.getClass().getCanonicalName());
            return (CodeGenerator) generatorBean;
        } else {
            String message = "Generator bean (" + generatorBean.getClass().getCanonicalName() + ") is not implementing interface " + CodeGenerator.class.getCanonicalName();
            throw new InvalidConfigProperty(message);
        }
    }

    public long getDefaultTtl() {
        return appProperties.getTtl() != null ? appProperties.getTtl() : 3600 * 24 * 365L;
    }

}
