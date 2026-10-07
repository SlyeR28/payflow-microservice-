package com.payflow.merchantservice.mapper;

import com.payflow.merchantservice.model.entity.GatewayCredentials;
import com.payflow.merchantservice.payload.responseDto.GatewayCredentialsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GatewayCredentialsMapper {

    @Mapping(target = "maskedApiKey", ignore = true)
    GatewayCredentialsResponse toResponse(GatewayCredentials credentials);

    List<GatewayCredentialsResponse> toResponseList(List<GatewayCredentials> credentialsList);
}
