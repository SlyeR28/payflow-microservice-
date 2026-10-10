package com.payflow.merchantservice.mapper;

import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.payload.requestDto.CreateMerchantRequest;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "panNumberMasked", ignore = true)
    @Mapping(target = "panNumberEncrypted", ignore = true)
    @Mapping(target = "panNumberHash", ignore = true)
    @Mapping(target = "isPanVerified", ignore = true)
    @Mapping(target = "panVerifiedAt", ignore = true)
    @Mapping(target = "isBankVerified", ignore = true)
    @Mapping(target = "bankVerifiedAt", ignore = true)
    @Mapping(target = "rejectReason", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "kycDocuments", ignore = true)
    @Mapping(target = "bankAccounts", ignore = true)
    @Mapping(target = "gatewayConfigs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Merchant toEntity(CreateMerchantRequest request);

    @Mapping(target = "addresses", ignore = true)
    MerchantResponse toResponse(Merchant merchant);
}
