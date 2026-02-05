package com.project.urlshortener.repository;

import com.project.urlshortener.model.LinkAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkAccessRepository extends JpaRepository<LinkAccessLog,Long> {


}
