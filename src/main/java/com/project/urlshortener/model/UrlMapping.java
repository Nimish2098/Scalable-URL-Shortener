package com.project.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name ="url")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UrlMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortCode;

    private String longUrl;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Long clickCount;
}
