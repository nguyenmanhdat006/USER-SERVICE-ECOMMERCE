package com.ecommerce.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthRequest {

    @NotBlank(message = "Authorization code is required")
    private String code;

    @NotBlank(message = "Redirect URI is required")
    private String redirectUri;
}
