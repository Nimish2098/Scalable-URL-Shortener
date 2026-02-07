package com.project.urlshortener.repository;

import com.project.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    public Optional<UrlMapping> findByShortCode(String code);

    public Optional<UrlMapping> findFirstByShortCode(String code);

    public java.util.List<UrlMapping> findByTrackingTag(String trackingTag);

    public boolean existsByShortCode(String code);

}
