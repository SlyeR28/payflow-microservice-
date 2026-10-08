package com.payflow.merchantservice.mapper;

import com.payflow.merchantservice.model.entity.MerchantAddress;
import com.payflow.merchantservice.payload.requestDto.AddressRequest;
import com.payflow.merchantservice.payload.responseDto.AddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantAddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "merchant", ignore = true)
    @Mapping(target = "addressHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MerchantAddress toEntity(AddressRequest request);

    AddressResponse toResponse(MerchantAddress address);
}
