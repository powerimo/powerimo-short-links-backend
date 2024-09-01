package org.powerimo.shortlinks.server.persistance.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
