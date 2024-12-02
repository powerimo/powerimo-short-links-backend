package org.powerimo.shortlinks.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkRequest {
    private String url;
    private Long ttl;
    private Long limitHits;
}
