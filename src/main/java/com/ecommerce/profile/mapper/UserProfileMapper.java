package com.ecommerce.profile.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.ecommerce.profile.dto.request.ProfileCreationRequest;
import com.ecommerce.profile.dto.request.UpdateProfileRequest;
import com.ecommerce.profile.dto.response.UserProfileResponse;
import com.ecommerce.profile.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfile toUserProfile(ProfileCreationRequest request);

    UserProfileResponse toUserProfileResponse(UserProfile entity);

    void update(@MappingTarget UserProfile entity, UpdateProfileRequest request);
}
