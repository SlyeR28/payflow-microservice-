package com.payflow.merchantservice.service;

import com.payflow.merchantservice.payload.requestDto.AddressRequest;
import com.payflow.merchantservice.payload.responseDto.AddressResponse;

import java.util.List;

public interface MerchantAddressService {

    AddressResponse addAddress(Long merchantId, AddressRequest request);

    List<AddressResponse> getMerchantAddresses(Long merchantId);

    AddressResponse getAddressById(Long addressId);

    AddressResponse updateAddress(Long addressId, AddressRequest request);

    void deleteAddress(Long addressId);
}
