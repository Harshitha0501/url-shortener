package com.emergent.urlshortener.repository;

import com.emergent.urlshortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    List<ClickEvent> findTop100ByShortCodeOrderByClickedAtDesc(String shortCode);

    long countByShortCode(String shortCode);

    @Query("SELECT c.deviceType AS key, COUNT(c) AS value FROM ClickEvent c WHERE c.shortCode = :code GROUP BY c.deviceType")
    List<Object[]> countByDevice(@Param("code") String shortCode);

    @Query("SELECT c.browser AS key, COUNT(c) AS value FROM ClickEvent c WHERE c.shortCode = :code GROUP BY c.browser")
    List<Object[]> countByBrowser(@Param("code") String shortCode);

    @Query("SELECT c.os AS key, COUNT(c) AS value FROM ClickEvent c WHERE c.shortCode = :code GROUP BY c.os")
    List<Object[]> countByOs(@Param("code") String shortCode);

    @Query("SELECT c.countryCode AS key, COUNT(c) AS value FROM ClickEvent c WHERE c.shortCode = :code GROUP BY c.countryCode")
    List<Object[]> countByCountry(@Param("code") String shortCode);

    @Query("SELECT c.referrer AS key, COUNT(c) AS value FROM ClickEvent c WHERE c.shortCode = :code GROUP BY c.referrer")
    List<Object[]> countByReferrer(@Param("code") String shortCode);
}
