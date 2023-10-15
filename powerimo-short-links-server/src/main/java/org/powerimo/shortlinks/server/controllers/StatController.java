package org.powerimo.shortlinks.server.controllers;

import lombok.RequiredArgsConstructor;
import org.powerimo.shortlinks.server.dto.StatResponse;
import org.powerimo.shortlinks.server.services.StatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("stat")
@RequiredArgsConstructor
public class StatController {
    private final StatService statService;

    @GetMapping
    public StatResponse getStat() {
        return statService.getSnapshot();
    }
}
