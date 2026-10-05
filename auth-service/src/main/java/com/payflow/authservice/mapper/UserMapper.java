package com.payflow.authservice.mapper;

import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.payload.responseDto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Map User entity → UserResponse DTO.
     * Field names match 1:1, so no @Mapping annotations needed.
     * Fields not present in the DTO (failedAttempts, lockedUntil, lastLoginAt)
     * are simply ignored.
     */
    @Mapping(target = "isUsernameTemporary", expression = "java(user.getUserNameChangedAt() == null)")
    UserResponse toResponse(User user);

}
