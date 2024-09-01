package org.powerimo.shortlinks.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkInfo {
    private String code;
    private String url;
    private Long hitCount;
    private Instant createdAt;
    private Instant expireAt;
    private Long ttl;
    private Long hitLimit;
}
