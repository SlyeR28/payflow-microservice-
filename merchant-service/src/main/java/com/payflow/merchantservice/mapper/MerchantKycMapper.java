package com.payflow.merchantservice.mapper;

import com.payflow.merchantservice.model.entity.MerchantKyc;
import com.payflow.merchantservice.payload.responseDto.KycDocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantKycMapper {

    KycDocumentResponse toResponse(MerchantKyc kyc);

    List<KycDocumentResponse> toResponseList(List<MerchantKyc> kycList);
}
