package com.ecommerce.userservice.dto.request;

import com.ecommerce.userservice.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAddressRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "^(\\+84|0)[0-9]{9}$",
            message = "Phone number must be valid Vietnamese phone number"
    )
    private String phone;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 500)
    private String addressLine1;

    @Size(max = 500)
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String district;

    @Size(max = 100)
    private String ward;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country;

    private Boolean isDefault;

    private Address.AddressType addressType;
}