package com.uniai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used by the front-end to request an authorization URL.
 * - redirectUri: optional override for the redirect URI to be embedded in the auth URL.
 * - state: optional state parameter that will be passed to Google and returned to the redirect URI.
 *
 * Uses Lombok to remove boilerplate and Jakarta Validation to allow controller-side validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleAuthUrlRequestDto {

    /**
     * Optional redirect URI override. Keep reasonably bounded to avoid abuse.
     */
    @JsonProperty("redirectUri")
    @Size(max = 2048)
    private String redirectUri;

    /**
     * Optional state string to carry client-side data through the OAuth flow.
     */
    @JsonProperty("state")
    @Size(max = 512)
    private String state;
}