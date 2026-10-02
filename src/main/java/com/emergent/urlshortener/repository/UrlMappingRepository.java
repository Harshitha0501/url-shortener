package com.emergent.urlshortener.repository;

import com.emergent.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :code")
    int incrementClickCount(@Param("code") String shortCode);

    @Modifying
    @Query("DELETE FROM UrlMapping u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now);
}
