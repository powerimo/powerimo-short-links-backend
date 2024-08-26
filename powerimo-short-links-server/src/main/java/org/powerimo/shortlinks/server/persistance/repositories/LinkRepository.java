package org.powerimo.shortlinks.server.persistance.repositories;

import org.powerimo.shortlinks.server.persistance.entities.LinkEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.Optional;

public interface LinkRepository extends CrudRepository<LinkEntity, String> {

    Optional<LinkEntity> findFirstByCode(String code);
    Optional<LinkEntity> findFirstByUrlHash(String hash);

    @Query(nativeQuery = true, value = "update link set hit_count=hit_count+1 where code=?")
    @Modifying
    void incrementGetCount(String code);

    @Query(nativeQuery = true, value = "select count(*) as res from link where created_at>=?")
    Integer getCreatedCount(Timestamp since);

    @Query(nativeQuery = true, value = "delete from link where expired_at <= ?")
    @Modifying
    void deleteExpiredLinks(Timestamp maxDate);
}
