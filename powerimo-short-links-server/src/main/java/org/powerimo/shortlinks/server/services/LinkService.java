package org.powerimo.shortlinks.server.services;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.powerimo.shortlinks.server.AppConst;
import org.powerimo.shortlinks.server.config.AppConfig;
import org.powerimo.shortlinks.server.dto.LinkRequest;
import org.powerimo.shortlinks.server.events.LinkHitEvent;
import org.powerimo.shortlinks.server.exceptions.InvalidArgument;
import org.powerimo.shortlinks.server.persistance.entities.LinkEntity;
import org.powerimo.shortlinks.server.persistance.entities.LinkHitEntity;
import org.powerimo.shortlinks.server.persistance.repositories.LinkHitRepository;
import org.powerimo.shortlinks.server.persistance.repositories.LinkRepository;
import org.powerimo.shortlinks.server.support.AppUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LinkService implements ApplicationListener<LinkHitEvent> {
    private final AppConfig appConfig;
    private final LinkRepository linkRepository;
    private final LinkHitRepository linkHitRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String addLink(String url) {
        var data = add(LinkRequest.builder()
                .url(url)
                .ttl(appConfig.getDefaultTtl())
                .build());
        return appConfig.getDomain() + "/" + data.getCode();
    }

    public LinkEntity add(@NonNull LinkRequest request) {
        log.debug("link request: {}", request);
        if (request.getUrl() == null)
            throw new InvalidArgument("URL must be not empty");
        request.setUrl(request.getUrl().trim());
        if (!isValidUrl(request.getUrl())) {
            throw new InvalidArgument("URL is not valid");
        }

        var hash = calculateHash(request.getUrl());
        log.debug("link hash: {} : {}",hash, request.getUrl() );

        // check the url is exists
        var hashEntityOpt = linkRepository.findFirstByUrlHash(hash);
        log.info("link exists: {} by hash: {}", hashEntityOpt.isPresent(), hash);
        if (hashEntityOpt.isPresent()) {
            return hashEntityOpt.get();
        }

        var code = createCode();
        if (request.getTtl() == 0) {
            request.setTtl(appConfig.getDefaultTtl());
        }

        var entity = LinkEntity.builder()
                .code(code)
                .url(request.getUrl())
                .urlHash(hash)
                .ttl(request.getTtl())
                .expiredAt(Instant.now().plus(request.getTtl(), ChronoUnit.SECONDS))
                .build();
        entity = linkRepository.save(entity);
        log.info("link entity added: {}", entity);
        return entity;
    }

    public String createCode() {
        int tryCount = 0;
        boolean isFree = false;
        String code = null;

        while (tryCount < 100 && !isFree) {
            tryCount++;
            code = generateCode();
            isFree = linkRepository.findFirstByCode(code).isEmpty();
            log.debug("code generation: attempt={}; code={}; isFree={}", code, tryCount, isFree);
        }

        if (!isFree) {
            throw new RuntimeException("There is no free codes");
        }
        if (code == null)
            throw new RuntimeException("Exception on generation code: code is null");

        return code;
    }

    public String generateCode() {
        SecureRandom random = new SecureRandom();
        int length = random.nextInt(8) + 1; // Длина строки от 1 до 8
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }

    @SneakyThrows
    public String calculateHash(String s) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedHash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String getRealLink(String code) {
        var opt = linkRepository.findFirstByCode(code);
        if (opt.isPresent()) {
            linkRepository.incrementGetCount(code);
            log.info("link get: {}", code);
            return opt.get().getUrl();
        } else {
            return noLinkUrl(code);
        }
    }

    public String hitLink(String code, HttpServletRequest request) {
        var opt = linkRepository.findFirstByCode(code);
        if (opt.isPresent()) {
            var userAgent = request.getHeader("User-Agent");
            var xForwadedFor = request.getHeader("x-forwarded-for");
            var remoteHost = xForwadedFor != null ? xForwadedFor : request.getRemoteHost();
            applicationEventPublisher.publishEvent(new LinkHitEvent(this, code, userAgent, remoteHost));
            log.info("link hit: {}", code);
            return opt.get().getUrl();
        } else {
            return noLinkUrl(code);
        }
    }

    private void addHitLinkEntity(String code, String agentString, String remoteHost) {
        UserAgent userAgent = new UserAgent(agentString);
        var osName = userAgent.getOperatingSystem() != null ? userAgent.getOperatingSystem().getName() : null;
        var browser = userAgent.getBrowser() != null ? userAgent.getBrowser().getName() : null;
        var browserVersion = userAgent.getBrowserVersion() != null ? userAgent.getBrowserVersion().getVersion() : null;

        var entity = LinkHitEntity.builder()
                .code(code)
                .host(remoteHost)
                .agentString(agentString)
                .extractedOsVersion(osName)
                .extractedBrowser(browser)
                .extractedBrowserVersion(browserVersion)
                .build();
        entity = linkHitRepository.save(entity);
        log.info("LinkHitEntity added: {}", entity);
    }

    public String noLinkUrl(String code) {
        return appConfig.getDomain() + appConfig.getNotFoundPath();
    }

    public static LinkRequest convert(LinkEntity entity) {
        return LinkRequest.builder()
                .ttl(entity.getTtl())
                .url(entity.getUrl())
                .build();
    }

    public boolean isValidUrl(String url) {
        try {
            // check the string could be used as URL object
            new URL(url);

            // check the link contains the correct protocol prefix
            if (appConfig.isOnlyHyperlinks()) {
                return url.regionMatches(true, 0, "http://", 0, 7) ||
                        url.regionMatches(true, 0, "https://", 0, 8);
            }
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public void onApplicationEvent(LinkHitEvent event) {
        log.trace("LinkHitEvent: {}", event);
        addHitLinkEntity(event.getCode(), event.getAgentString(), event.getRemoteHost());
        linkRepository.incrementGetCount(event.getCode());
    }

    public void cleanupExpired(String trigger) {
        log.debug("cleanupExpired: trigger={}; app.cleanup={}", trigger, appConfig.isCleanupEnabled());
        if (!appConfig.isCleanupEnabled()) {
            log.debug("Cleanup (app.cleanup) disabled in properties");
            return;
        }

        try {
            var maxDate = AppUtils.utcTimestamp(Instant.now());
            linkHitRepository.deleteExpiredLinkHits(maxDate);
            linkRepository.deleteExpiredLinks(maxDate);
            log.info("Cleanup performed. Expired timestamp: {}", maxDate);
        } catch (Exception ex) {
            log.error("Cleanup failed", ex);
        }
    }

}
