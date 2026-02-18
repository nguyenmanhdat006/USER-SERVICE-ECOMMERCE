package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.CreateAddressRequest;
import com.ecommerce.userservice.dto.response.AddressResponse;
import com.ecommerce.userservice.entity.Address;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.mapper.UserMapper;
import com.ecommerce.userservice.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Get all addresses for current user
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> getCurrentUserAddresses() {
        User user = userService.getCurrentUserEntity();
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        return userMapper.toAddressResponseList(addresses);
    }

    /**
     * Get address by ID
     */
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(UUID id) {
        User currentUser = userService.getCurrentUserEntity();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        // Ensure the address belongs to the current user
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        return userMapper.toAddressResponse(address);
    }

    /**
     * Create new address for current user
     */
    @Transactional
    public AddressResponse createAddress(CreateAddressRequest request) {
        User user = userService.getCurrentUserEntity();

        Address address = userMapper.toAddressEntity(request);
        address.setUser(user);

        // If this is the first address or marked as default, make it default
        List<Address> existingAddresses = addressRepository.findByUser(user);
        if (existingAddresses.isEmpty() || Boolean.TRUE.equals(request.getIsDefault())) {
            // Unset other default addresses
            addressRepository.unsetDefaultForUser(user);
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address created with ID: {}", savedAddress.getId());

        return userMapper.toAddressResponse(savedAddress);
    }

    /**
     * Update address
     */
    @Transactional
    public AddressResponse updateAddress(UUID id, CreateAddressRequest request) {
        User currentUser = userService.getCurrentUserEntity();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        // Ensure the address belongs to the current user
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        // Update fields
        if (request.getFullName() != null) address.setFullName(request.getFullName());
        if (request.getPhone() != null) address.setPhone(request.getPhone());
        if (request.getAddressLine1() != null) address.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) address.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict());
        if (request.getWard() != null) address.setWard(request.getWard());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getAddressType() != null) address.setAddressType(request.getAddressType());

        // Handle default flag
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.unsetDefaultForUser(currentUser);
            address.setIsDefault(true);
        }

        Address updatedAddress = addressRepository.save(address);
        log.info("Address {} updated", id);

        return userMapper.toAddressResponse(updatedAddress);
    }

    /**
     * Delete address
     */
    @Transactional
    public void deleteAddress(UUID id) {
        User currentUser = userService.getCurrentUserEntity();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        // Ensure the address belongs to the current user
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        // If this was the default address, set another one as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUser(currentUser);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }

        log.info("Address {} deleted", id);
    }

    /**
     * Set address as default
     */
    @Transactional
    public AddressResponse setDefaultAddress(UUID id) {
        User currentUser = userService.getCurrentUserEntity();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        // Ensure the address belongs to the current user
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        // Unset other default addresses
        addressRepository.unsetDefaultForUser(currentUser);

        // Set this address as default
        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);

        log.info("Address {} set as default", id);

        return userMapper.toAddressResponse(updatedAddress);
    }

    /**
     * Get default address for current user
     */
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress() {
        User user = userService.getCurrentUserEntity();
        Address address = addressRepository.findByUserAndIsDefaultTrue(user)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));

        return userMapper.toAddressResponse(address);
    }
}