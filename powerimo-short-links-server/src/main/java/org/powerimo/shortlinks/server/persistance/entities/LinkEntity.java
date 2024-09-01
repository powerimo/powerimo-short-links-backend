package org.powerimo.shortlinks.server.persistance.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.powerimo.shortlinks.server.dto.LinkInfo;

import java.time.Instant;

@Table(schema = "public", name = "link")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkEntity {
    @Id
    private String code;
    private String url;
    private String urlHash;
    private Long ttl;
    private Instant expireAt;
    private String host;
    private String identityValue;

    @Column(insertable = false, updatable = false)
    private Instant createdAt;

    @Column(insertable = false, updatable = false)
    private Instant updatedAt;

    private Long hitLimit;
    private Long hitCount;

    public LinkInfo asLinkInfo() {
        return LinkInfo.builder()
                .hitCount(this.hitCount)
                .hitLimit(this.hitLimit)
                .code(this.code)
                .expireAt(this.expireAt)
                .createdAt(this.createdAt)
                .url(this.url)
                .ttl(this.ttl)
                .hitLimit(this.hitLimit)
                .build();
    }
}
