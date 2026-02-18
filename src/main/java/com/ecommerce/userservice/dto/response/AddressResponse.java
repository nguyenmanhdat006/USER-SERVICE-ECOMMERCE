package com.ecommerce.userservice.dto.response;

import com.ecommerce.userservice.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private UUID id;
    private String fullName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String district;
    private String ward;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private Address.AddressType addressType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}