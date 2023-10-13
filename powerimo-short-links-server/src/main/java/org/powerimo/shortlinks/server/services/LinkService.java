package org.powerimo.shortlinks.server.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.powerimo.shortlinks.server.config.AppConfig;
import org.powerimo.shortlinks.server.dto.LinkRequest;
import org.powerimo.shortlinks.server.persistance.entities.LinkEntity;
import org.powerimo.shortlinks.server.persistance.repositories.LinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LinkService {
    private final AppConfig appConfig;
    private final LinkRepository linkRepository;
    private final Map<String, String> cacheLinks = new HashMap<>();
    private int counter = 0;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    public String addLink(String url) {
        var data =  add(LinkRequest.builder()
                .url(url)
                .ttl(appConfig.getDefaultTtl())
                .build());
        return appConfig.getDomain() + "/" + data.getCode();
    }

    public LinkEntity add(LinkRequest request) {
        log.debug("link request: {}", request);
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

    public boolean isLinkExists(String url) {
        return findLink(url).isPresent();
    }

    public Optional<Map.Entry<String, String>> findLink(String url) {
        return cacheLinks.entrySet().stream()
                .filter(item -> item.getValue().equals(url))
                .findFirst();
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
        byte[] encodedhash = digest.digest(
                s.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
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

    public String noLinkUrl(String code) {
        return "No link for: " + code;
    }

    public static LinkRequest convert(LinkEntity entity) {
        return LinkRequest.builder()
                .ttl(entity.getTtl())
                .url(entity.getUrl())
                .build();
    }
}
