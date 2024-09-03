package org.powerimo.shortlinks.server.services;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.powerimo.shortlinks.server.config.AppConfig;
import org.powerimo.shortlinks.server.config.AppProperties;
import org.powerimo.shortlinks.server.dto.LinkInfo;
import org.powerimo.shortlinks.server.dto.LinkRequest;
import org.powerimo.shortlinks.server.events.LinkHitEvent;
import org.powerimo.shortlinks.server.exceptions.InvalidArgument;
import org.powerimo.shortlinks.server.exceptions.NotFoundException;
import org.powerimo.shortlinks.server.generators.CodeGenerator;
import org.powerimo.shortlinks.server.persistance.entities.LinkEntity;
import org.powerimo.shortlinks.server.persistance.entities.LinkHitEntity;
import org.powerimo.shortlinks.server.persistance.repositories.LinkHitRepository;
import org.powerimo.shortlinks.server.persistance.repositories.LinkRepository;
import org.powerimo.shortlinks.server.support.AppUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
    private final CodeGenerator codeGenerator;
    private final AppProperties appProperties;

    public String addLink(String url, Long ttl, Long limitHits) {
        // if TTL is null. default will be applied
        var effectiveTtl = ttl == null ? appConfig.getDefaultTtl() : ttl;

        var data = add(LinkRequest.builder()
                .url(url)
                .ttl(effectiveTtl)
                .limitHits(limitHits)
                .build());
        return appProperties.getDomain() + "/" + data.getCode();
    }

    public LinkEntity add(@NonNull LinkRequest request) {
        log.info("link request: {}", request);
        if (request.getUrl() == null)
            throw new InvalidArgument("URL must be not empty");
        request.setUrl(request.getUrl().trim());
        if (!isValidUrl(request.getUrl())) {
            throw new InvalidArgument("URL is not valid");
        }

        var hash = calculateHash(request.getUrl());
        log.debug("link hash: {} : {}", hash, request.getUrl());

        // check the url exist
        var hashEntityOpt = linkRepository.findFirstByUrlHash(hash);
        log.info("link exists: {} by hash: {}", hashEntityOpt.isPresent(), hash);
        if (hashEntityOpt.isPresent()) {
            return hashEntityOpt.get();
        }

        var code = createCode(request.getUrl());
        if (request.getTtl() == 0) {
            log.debug("TTL is not specified. Default TTL will be used: {}. Request={}", appConfig.getDefaultTtl(), request);
            request.setTtl(appConfig.getDefaultTtl());
        }

        var entity = LinkEntity.builder()
                .code(code)
                .url(request.getUrl())
                .urlHash(hash)
                .ttl(request.getTtl())
                .expireAt(Instant.now().plus(request.getTtl(), ChronoUnit.SECONDS))
                .hitCount(0L)
                .hitLimit(request.getLimitHits())
                .build();
        entity = linkRepository.save(entity);
        log.info("link entity added: {}", entity);
        return entity;
    }

    public String createCode(String url) {
        int tryCount = 0;
        boolean isFree = false;
        String code = null;

        while (tryCount < 100 && !isFree) {
            tryCount++;
            code = codeGenerator.generate(url);
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

    /**
     * Method returns URL and register thi link hit. If the code is not found redirects to noLinkUrl method result.
     * @param code the link code
     * @param request ServletRequest from the controller
     * @return the URL of the link or noLinkUrl method result
     */
    public String hitLink(String code, HttpServletRequest request) {
        var opt = linkRepository.findFirstByCode(code);
        if (opt.isPresent()) {
            var linkEntity = opt.get();

            // check limits
            if (linkEntity.getExpireAt().isBefore(Instant.now())) {
                log.info("Link expired at: {}; LinkCode: {}", linkEntity.getExpireAt(), code);
                return noLinkUrl(code);
            }
            if (linkEntity.getHitLimit() != null && linkEntity.getHitCount() >= linkEntity.getHitLimit()) {
                log.info("Link hits limit is exhausted: LinkCode: {}", linkEntity.getCode());
                return noLinkUrl(code);
            }

            var userAgent = AppUtils.extractBrowserString(request);
            var remoteHost = AppUtils.extractRemoteIp(request);
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
        return appProperties.getDomain() + appConfig.getNotFoundPath();
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
            if (appProperties.isOnlyHyperlinks()) {
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
        log.debug("cleanupExpired: trigger={}; app.cleanup={}", trigger, appProperties.isCleanup());
        if (!appProperties.isCleanup()) {
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

    public LinkInfo getLinkInfo(@NonNull String code) {
        var entity = linkRepository.findFirstByCode(code).orElseThrow(() -> new NotFoundException("The link with code " + code + " was not found"));
        return entity.asLinkInfo();
    }

}
