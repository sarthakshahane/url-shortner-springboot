package com.urlshortner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RESPONSE DTO → what our API sends BACK to the client.
 *
 * We intentionally exclude sensitive/unnecessary fields from the entity.
 * The client gets exactly what they need — nothing more.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponse {

    private Long id;

    /**
     * The full shortened URL the user can share.
     * Example: "http://localhost:8080/aB3xYz"
     */
    private String shortUrl;

    private String originalUrl;

    private String shortCode;

    private Long clickCount;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}
