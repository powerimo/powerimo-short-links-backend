package org.powerimo.shortlinks.server.support;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;

public class AppUtils {

    public static Timestamp utcTimestamp(Instant date) {
        var d = date.atZone(ZoneOffset.UTC);
        return Timestamp.valueOf(d.toLocalDateTime());
    }

}
