package org.powerimo.shortlinks.server.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powerimo.shortlinks.api.LinkRequest;
import org.powerimo.shortlinks.server.config.AppConfig;
import org.powerimo.shortlinks.server.config.AppProperties;
import org.powerimo.shortlinks.server.exceptions.InvalidArgument;
import org.powerimo.shortlinks.server.generators.CodeGenerator;
import org.powerimo.shortlinks.server.generators.UuidCodeGenerator;
import org.powerimo.shortlinks.server.persistance.entities.LinkEntity;
import org.powerimo.shortlinks.server.persistance.repositories.LinkHitRepository;
import org.powerimo.shortlinks.server.persistance.repositories.LinkRepository;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestLinkService {
    @Mock
    private AppConfig appConfig;

    @Mock
    private LinkHitRepository linkHitRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private CodeGenerator codeGenerator = new UuidCodeGenerator();

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private LinkService linkService;

    private final static String DEFAULT_URL = "http://test.url/contextpath";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addLink_success() {
        // Мокаем необходимые зависимости
        String url = DEFAULT_URL;

        when(appConfig.getDefaultTtl()).thenReturn(60L);
        when(codeGenerator.generate(url)).thenReturn("abc123");

        LinkEntity linkEntity = LinkEntity.builder()
                .url(url)
                .ttl(10L)
                .code("abc123")
                .build();

        // Мокаем вызов репозитория для сохранения LinkEntity
        when(linkRepository.save(any(LinkEntity.class))).thenReturn(linkEntity);

        // Выполняем тестируемый метод
        String result = linkService.addLink(url, 10L, 200L);

        // Проверяем результат
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("abc123"));

        // Проверяем, что код был сгенерирован и сущность сохранена
        verify(codeGenerator).generate(url);
        verify(linkRepository).save(any(LinkEntity.class));
    }

    @Test
    void addLink_defTtl_success() {
        String url = DEFAULT_URL;

        when(appConfig.getDefaultTtl()).thenReturn(60L);
        when(codeGenerator.generate(url)).thenReturn("abc123");

        LinkEntity linkEntity = LinkEntity.builder()
                .url(url)
                .ttl(10L)
                .code("abc123")
                .build();

        when(linkRepository.save(any(LinkEntity.class))).thenReturn(linkEntity);

        String result = linkService.addLink(url, null, null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("abc123"));

        verify(codeGenerator).generate(url);
        verify(linkRepository).save(any(LinkEntity.class));
    }

    @Test
    void add_success() {
        LinkEntity linkEntity = LinkEntity.builder()
                .url(DEFAULT_URL)
                .ttl(10L)
                .code("abc123")
                .build();

        when(appConfig.getDefaultTtl()).thenReturn(60L);
        when(codeGenerator.generate(any())).thenReturn("abc123");
        when(linkRepository.save(any(LinkEntity.class))).thenReturn(linkEntity);

        LinkRequest linkRequest = LinkRequest.builder()
                .url(DEFAULT_URL)
                .ttl(12L)
                .build();

        var result = linkService.add(linkRequest);
        Assertions.assertNotNull(result);
    }

    @Test
    void add_emptyUrl() {
        LinkRequest linkRequest = LinkRequest.builder()
                .ttl(12L)
                .build();
        Assertions.assertThrows(InvalidArgument.class, () -> linkService.add(linkRequest));
    }

    @Test
    void add_nonUrl() {
        LinkRequest linkRequest = LinkRequest.builder()
                .url("non-url-string")
                .ttl(12L)
                .build();
        Assertions.assertThrows(InvalidArgument.class, () -> linkService.add(linkRequest));
    }

}
