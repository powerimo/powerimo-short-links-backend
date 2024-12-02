package org.powerimo.shortlinks.server.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.powerimo.shortlinks.api.LinkRequest;
import org.powerimo.shortlinks.server.config.AppConfig;
import org.powerimo.shortlinks.server.config.AppProperties;
import org.powerimo.shortlinks.server.services.LinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class LinksController {
    private final LinkService linkService;
    private final AppConfig appConfig;
    private final AppProperties appProperties;

    @PostMapping
    public String postLink(@RequestBody String url,
                           @RequestParam(required = false, name = "ttl") Long ttl,
                           @RequestParam(required = false, name = "limit_hits") Long limitHits) {
        return linkService.addLink(url, ttl, limitHits);
    }

    @PostMapping("request")
    public LinkRequest postRequest(@RequestBody LinkRequest linkRequest) {
        var data = linkService.add(linkRequest);
        return LinkService.convert(data);
    }

    @GetMapping
    public void getNotFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var frontendUrl = request.getRequestURL().toString() + appProperties.getFrontendContextPath();
        response.sendRedirect(frontendUrl);
    }

    @GetMapping("{code}")
    public void getLink(HttpServletRequest request,
                        HttpServletResponse response,
                        @PathVariable String code) throws IOException {
        var real = linkService.hitLink(code, request);
        response.sendRedirect(real);
    }

    @GetMapping("info/{code}")
    public ResponseEntity<?> getInfoCode(HttpServletRequest request,
                                      @PathVariable String code) {
        try {
            var data = linkService.getLinkInfo(code);
            return ResponseEntity.ok(data);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

}
