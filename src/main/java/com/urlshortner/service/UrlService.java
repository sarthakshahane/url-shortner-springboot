package com.urlshortner.service;

import com.urlshortner.dto.UrlRequest;
import com.urlshortner.dto.UrlResponse;
import com.urlshortner.entity.Url;
import com.urlshortner.exception.ShortCodeAlreadyExistsException;
import com.urlshortner.exception.UrlExpiredException;
import com.urlshortner.exception.UrlNotFoundException;
import com.urlshortner.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE LAYER = Business Logic
 *
 * This is where the actual work happens.
 * The Controller just receives HTTP requests and delegates to the Service.
 * The Repository just does DB operations.
 * The Service coordinates between them and enforces business rules.
 *
 * @Service → Spring registers this as a bean (a managed object).
 *   Spring will inject it wherever it's needed via @Autowired or constructor injection.
 *
 * @RequiredArgsConstructor (Lombok) → generates a constructor for all 'final' fields.
 *   Spring sees that constructor and injects the dependencies automatically.
 *   This is CONSTRUCTOR INJECTION — the recommended approach over @Autowired on fields.
 *
 * @Transactional → wraps a method in a database transaction.
 *   If anything throws an exception inside, the whole operation is rolled back.
 *   readOnly = true → tells Hibernate "no writes here", enabling read optimizations.
 */
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    // Reads 'app.base-url' from application.properties
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length}")
    private int shortCodeLength;

    // Characters used to generate random short codes
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // SecureRandom is safer than Random (cryptographically secure)
    private final SecureRandom random = new SecureRandom();

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE: Shorten a URL
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public UrlResponse shortenUrl(UrlRequest request) {

        // 1. If this URL was already shortened, return the existing one (idempotent)
        var existing = urlRepository.findByOriginalUrl(request.getOriginalUrl());
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        // 2. Determine short code (custom or auto-generated)
        String shortCode;
        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            // User wants a custom code — check it's not taken
            if (urlRepository.existsByShortCode(request.getCustomCode())) {
                throw new ShortCodeAlreadyExistsException(
                    "Short code '" + request.getCustomCode() + "' is already taken. Try a different one."
                );
            }
            shortCode = request.getCustomCode();
        } else {
            // Auto-generate a unique code
            shortCode = generateUniqueShortCode();
        }

        // 3. Build and save the entity
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .clickCount(0L)
                .build();

        Url savedUrl = urlRepository.save(url);

        return mapToResponse(savedUrl);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ: Get original URL by short code (used for redirect)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                    "Short code '" + shortCode + "' not found"
                ));

        // Check if URL has expired
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("This short URL has expired");
        }

        // Increment click count atomically (single SQL UPDATE, no race conditions)
        urlRepository.incrementClickCount(shortCode);

        return url.getOriginalUrl();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ: Get URL stats by short code
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public UrlResponse getUrlStats(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                    "Short code '" + shortCode + "' not found"
                ));
        return mapToResponse(url);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ: Get all URLs
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<UrlResponse> getAllUrls() {
        return urlRepository.findAll()
                .stream()
                .map(this::mapToResponse)   // equivalent to url -> mapToResponse(url)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE: Remove a short URL
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public void deleteUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                    "Short code '" + shortCode + "' not found"
                ));
        urlRepository.delete(url);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Generate a unique random short code
    // ─────────────────────────────────────────────────────────────────────────
    private String generateUniqueShortCode() {
        String code;
        // Keep trying until we find a code that doesn't exist in DB
        do {
            code = generateRandomCode();
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(shortCodeLength);
        for (int i = 0; i < shortCodeLength; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAPPER: Convert Url entity → UrlResponse DTO
    // ─────────────────────────────────────────────────────────────────────────
    private UrlResponse mapToResponse(Url url) {
        return UrlResponse.builder()
                .id(url.getId())
                .shortUrl(baseUrl + "/" + url.getShortCode())  // full redirect URL
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }
}
