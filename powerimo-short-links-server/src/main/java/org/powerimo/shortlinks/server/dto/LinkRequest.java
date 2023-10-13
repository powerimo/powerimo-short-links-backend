package org.powerimo.shortlinks.server.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkRequest {
    private String url;
    private int ttl;
}
