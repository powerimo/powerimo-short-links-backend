package org.powerimo.shortlinks.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatResponse {
    private int linkCreated;
    private int linkHits;
}
