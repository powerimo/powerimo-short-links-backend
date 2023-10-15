package org.powerimo.shortlinks.server.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.powerimo.shortlinks.server.AppConst;
import org.powerimo.shortlinks.server.services.LinkService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {
    private final LinkService linkService;

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 1L)
    public void cleanupExpired() {
        log.info("Planned cleanup initiated");
        linkService.cleanupExpired(AppConst.TRIGGER_SCHEDULER);
    }

}
