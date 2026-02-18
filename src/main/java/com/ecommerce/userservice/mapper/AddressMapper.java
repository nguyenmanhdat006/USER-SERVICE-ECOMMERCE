package com.ecommerce.userservice.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import com.ecommerce.userservice.dto.request.CreateAddressRequest;
import com.ecommerce.userservice.dto.response.AddressResponse;
import com.ecommerce.userservice.entity.Address;
@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddress(CreateAddressRequest request);
    AddressResponse toAddressResponse(Address entity);
    void update(@MappingTarget Address entity, CreateAddressRequest request);
}
