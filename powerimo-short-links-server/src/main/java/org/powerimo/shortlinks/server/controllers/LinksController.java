package org.powerimo.shortlinks.server.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.powerimo.shortlinks.server.AppConst;
import org.powerimo.shortlinks.server.dto.LinkRequest;
import org.powerimo.shortlinks.server.services.LinkService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LinksController {
    private final LinkService linkService;

    @PostMapping
    public String postLink(@RequestBody String url) {
        return linkService.addLink(url);
    }

    @PostMapping("request")
    public LinkRequest postRequest(@RequestBody LinkRequest linkRequest) {
        var data = linkService.add(linkRequest);
        return LinkService.convert(data);
    }

    @GetMapping
    public void getNotFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var frontendUrl = request.getRequestURL().toString() + "/frontend";
        response.sendRedirect(frontendUrl);
    }

    @GetMapping("{code}")
    public void getLink(HttpServletRequest request,
                        HttpServletResponse response,
                        @PathVariable String code) throws IOException {
        var real = linkService.hitLink(code, request);
        response.sendRedirect(real);
    }

}
