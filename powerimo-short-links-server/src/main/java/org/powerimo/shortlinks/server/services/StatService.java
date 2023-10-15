package org.powerimo.shortlinks.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.powerimo.shortlinks.server.config.AppConfig;
import org.powerimo.shortlinks.server.dto.StatResponse;
import org.powerimo.shortlinks.server.persistance.repositories.LinkHitRepository;
import org.powerimo.shortlinks.server.persistance.repositories.LinkRepository;
import org.powerimo.shortlinks.server.support.AppUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatService {
    private final LinkRepository linkRepository;
    private final LinkHitRepository linkHitRepository;
    private final AppConfig appConfig;

    public int getCreatedCount(int intervalMinutes) {
        var t = utcSinceShiftedMinutes(intervalMinutes);
        var data = linkRepository.getCreatedCount(t);
        log.trace("getCreated(intervalMinutes={}) => timestamp={}; result={}", intervalMinutes, t, data);
        return data;
    }

    public int getRedirectsCount(int intervalMinutes) {
        var t = utcSinceShiftedMinutes(intervalMinutes);
        var data = linkHitRepository.getCreatedCount(t);
        log.trace("getCreated(intervalMinutes={}) => timestamp={}; result={}", intervalMinutes, t, data);
        return data;
    }

    public StatResponse getSnapshot() {
        StatResponse response = new StatResponse();
        var interval = appConfig.getStatInterval();
        response.setLinkCreated(getCreatedCount(interval));
        response.setLinkHits(getRedirectsCount(interval));
        log.trace("stat snapshot created: interval={}, data={}", interval, response);
        return response;
    }

    public static Timestamp utcSinceShiftedMinutes(int intervalMinutes) {
        return AppUtils.utcTimestamp(Instant.now().minus(intervalMinutes, ChronoUnit.MINUTES));
    }

}
