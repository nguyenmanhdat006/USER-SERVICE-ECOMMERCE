package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.request.CreateAddressRequest;
import com.ecommerce.userservice.dto.response.AddressResponse;
import com.ecommerce.userservice.dto.response.ApiResponse;
import com.ecommerce.userservice.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Address", description = "Address Management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get all addresses", description = "Get all addresses for current user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses() {
        List<AddressResponse> addresses = addressService.getCurrentUserAddresses();
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", description = "Get address by ID")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(@PathVariable UUID id) {
        AddressResponse address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @GetMapping("/default")
    @Operation(summary = "Get default address", description = "Get default address for current user")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress() {
        AddressResponse address = addressService.getDefaultAddress();
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PostMapping
    @Operation(summary = "Create address", description = "Create new address for current user")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody CreateAddressRequest request) {
        log.info("Create address request received");
        AddressResponse address = addressService.createAddress(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Update address by ID")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAddressRequest request) {
        log.info("Update address request received for ID: {}", id);
        AddressResponse address = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set default address", description = "Set address as default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(@PathVariable UUID id) {
        log.info("Set default address request received for ID: {}", id);
        AddressResponse address = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Default address updated", address));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Delete address by ID")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable UUID id) {
        log.info("Delete address request received for ID: {}", id);
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }
}