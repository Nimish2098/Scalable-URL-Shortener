package com.project.urlshortener.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="resume_access_log")
@Getter
@Setter
public class LinkAccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tag;
    private String ipAddress;
    private String location;
    private String userAgent;
    private LocalDateTime accessedAt;

}
