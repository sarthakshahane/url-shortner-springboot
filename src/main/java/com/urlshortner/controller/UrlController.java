package com.urlshortner.controller;

import com.urlshortner.dto.UrlRequest;
import com.urlshortner.dto.UrlResponse;
import com.urlshortner.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * CONTROLLER LAYER = HTTP Request/Response handling
 *
 * This class only handles:
 *   1. Receiving HTTP requests
 *   2. Passing data to the Service
 *   3. Building and returning HTTP responses
 *
 * It should contain ZERO business logic — that all lives in UrlService.
 *
 * @RestController → combines @Controller + @ResponseBody.
 *   Every method automatically serializes the return value to JSON.
 *
 * @RequestMapping("/api/urls") → base path for all endpoints in this class.
 *
 * @RequiredArgsConstructor → constructor injection of UrlService.
 *
 * Swagger Annotations:
 *   @Tag        → groups endpoints in Swagger UI
 *   @Operation  → describes a single endpoint
 *   @ApiResponses → documents possible HTTP response codes
 */
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "APIs for creating and managing short URLs")
public class UrlController {

    private final UrlService urlService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/urls → Create a short URL
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @RequestBody → deserializes the JSON request body into a UrlRequest object
     * @Valid       → triggers validation annotations on UrlRequest (@NotBlank, @Pattern, etc.)
     *               If validation fails, MethodArgumentNotValidException is thrown
     *               and caught by GlobalExceptionHandler → returns 400 Bad Request
     *
     * ResponseEntity<UrlResponse> → we control both the body AND the HTTP status code
     * HttpStatus.CREATED (201) → standard "resource was created successfully"
     */
    @PostMapping
    @Operation(summary = "Shorten a URL", description = "Creates a short URL for the given original URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Short URL created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid URL format"),
        @ApiResponse(responseCode = "409", description = "Custom short code already taken")
    })
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody UrlRequest request) {
        UrlResponse response = urlService.shortenUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/urls → Get all short URLs
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Get all URLs", description = "Returns a list of all shortened URLs and their stats")
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        return ResponseEntity.ok(urlService.getAllUrls());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/urls/{shortCode}/stats → Get stats for one URL
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @PathVariable → binds the {shortCode} in the URL path to the method parameter.
     * Example: GET /api/urls/aB3xYz/stats → shortCode = "aB3xYz"
     */
    @GetMapping("/{shortCode}/stats")
    @Operation(summary = "Get URL stats", description = "Returns click count and metadata for a short URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stats retrieved"),
        @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public ResponseEntity<UrlResponse> getUrlStats(
            @Parameter(description = "The short code to look up")
            @PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getUrlStats(shortCode));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/urls/{shortCode} → Delete a short URL
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Delete a short URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        urlService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();   // 204 No Content
    }
}
