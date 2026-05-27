package com.urlshortner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ENTITY = a Java class that maps to a database table.
 *
 * @Entity   → tells Hibernate: "manage this class as a DB table"
 * @Table    → specifies the table name (optional if class name matches)
 * @Id      → marks the primary key field
 * @GeneratedValue → auto-increment (IDENTITY = uses DB's serial/auto_increment)
 * @Column  → maps field to a specific column name
 *
 * @Data        (Lombok) → generates getters, setters, toString, equals, hashCode
 * @Builder     (Lombok) → enables builder pattern: URL.builder().shortCode("abc").build()
 * @NoArgsConstructor   → generates no-arg constructor (required by JPA/Hibernate)
 * @AllArgsConstructor  → generates constructor with all fields
 */
@Entity
@Table(name = "urls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The original long URL the user submitted.
     * Example: "https://www.google.com/search?q=spring+boot"
     */
    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    /**
     * The random 6-character code we generate.
     * Example: "aB3xYz"
     * This is what appears in: http://localhost:8080/aB3xYz
     */
    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    /**
     * How many times this short URL has been visited.
     */
    @Column(name = "click_count")
    @Builder.Default
    private Long clickCount = 0L;

    /**
     * When this record was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Optional expiry date. If null, URL never expires.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * @PrePersist runs BEFORE the entity is saved to DB for the first time.
     * This sets createdAt automatically — we never have to set it manually.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
