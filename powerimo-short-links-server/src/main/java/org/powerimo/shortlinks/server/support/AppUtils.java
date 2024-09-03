package org.powerimo.shortlinks.server.support;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;

public class AppUtils {

    public static Timestamp utcTimestamp(Instant date) {
        var d = date.atZone(ZoneOffset.UTC);
        return Timestamp.valueOf(d.toLocalDateTime());
    }

    public static String extractRemoteIp(@NonNull HttpServletRequest request) {
        var xForwardedFor = extractForwardedFor(request);
        return xForwardedFor != null ? xForwardedFor : request.getRemoteHost();
    }

    public static String extractBrowserString(@NonNull HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static String extractForwardedFor(@NonNull HttpServletRequest request) {
        return request.getHeader("x-forwarded-for");
    }
}
