package com.urlshortner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO = Data Transfer Object
 *
 * WHY USE DTOs?
 * - Never expose your Entity directly to the API.
 * - The Entity is your DB structure. The DTO is your API contract.
 * - You can have fields in the DB that you don't want in the API response (passwords, internal IDs).
 * - You can validate input on the DTO before it ever touches your service/DB.
 *
 * REQUEST DTO → what the CLIENT sends TO our API
 */
@Data
public class UrlRequest {

    /**
     * @NotBlank → fails validation if the field is null, empty, or only whitespace
     * @Pattern  → validates the URL format using regex
     *
     * These annotations work with @Valid in the Controller.
     */
    @NotBlank(message = "Original URL cannot be blank")
    @Pattern(
        regexp = "^(https?://).*",
        message = "URL must start with http:// or https://"
    )
    private String originalUrl;

    /**
     * Optional: user can request a custom short code like "mylink"
     * If null, we auto-generate one.
     */
    private String customCode;
}
