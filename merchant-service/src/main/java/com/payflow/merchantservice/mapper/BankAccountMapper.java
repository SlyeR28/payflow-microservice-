package com.payflow.merchantservice.mapper;

import com.payflow.merchantservice.model.entity.BankAccount;
import com.payflow.merchantservice.payload.responseDto.BankAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BankAccountMapper {

    @Mapping(target = "merchantId", source = "merchant.id")
    BankAccountResponse toResponse(BankAccount bankAccount);


    List<BankAccountResponse> toResponseList(List<BankAccount> bankAccounts);
}
