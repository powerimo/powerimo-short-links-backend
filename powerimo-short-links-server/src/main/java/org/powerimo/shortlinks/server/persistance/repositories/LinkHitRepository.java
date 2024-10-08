package org.powerimo.shortlinks.server.persistance.repositories;

import org.powerimo.shortlinks.server.persistance.entities.LinkHitEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;

public interface LinkHitRepository extends CrudRepository<LinkHitEntity, Long> {

    @Query(nativeQuery = true, value = "select count(*) as res from link_hit where created_at>=?")
    Integer getCreatedCount(Timestamp since);

    @Query(nativeQuery = true, value = "delete link_hit where code in (select code from link where expired_at <= ?)")
    @Modifying
    void deleteExpiredLinkHits(Timestamp maxDate);
}
