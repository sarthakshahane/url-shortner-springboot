package com.urlshortner.controller;

import com.urlshortner.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * REDIRECT CONTROLLER
 *
 * Handles the actual redirect: when a user visits http://localhost:8080/aB3xYz,
 * this controller looks up the original URL and sends them there.
 *
 * Kept SEPARATE from UrlController because:
 *   - Different base path (root "/" vs "/api/urls")
 *   - Different responsibility: this is the "public" endpoint users click on
 *     while UrlController is the "admin/API" endpoint developers call
 */
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    /**
     * GET /{shortCode} → Redirect to original URL
     *
     * HTTP 302 Found = temporary redirect.
     * The browser sees the Location header and automatically navigates there.
     *
     * Flow:
     *   1. User visits http://localhost:8080/aB3xYz
     *   2. We look up "aB3xYz" in the DB
     *   3. We increment click count
     *   4. We respond with 302 + Location: https://original-long-url.com
     *   5. Browser redirects automatically
     */
    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
        @ApiResponse(responseCode = "404", description = "Short code not found"),
        @ApiResponse(responseCode = "410", description = "URL has expired")
    })
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));   // Set the redirect destination

        return new ResponseEntity<>(headers, HttpStatus.FOUND);  // 302 Found
    }
}
