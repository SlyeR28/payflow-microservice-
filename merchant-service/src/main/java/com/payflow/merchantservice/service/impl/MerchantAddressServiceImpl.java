package com.payflow.merchantservice.service.impl;

import com.payflow.merchantservice.mapper.MerchantAddressMapper;
import com.payflow.merchantservice.payload.requestDto.AddressRequest;
import com.payflow.merchantservice.payload.responseDto.AddressResponse;
import com.payflow.merchantservice.repository.MerchantAddressRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.MerchantAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MerchantAddressServiceImpl implements MerchantAddressService {

    private final MerchantRepository merchantRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final MerchantAddressMapper merchantAddressMapper;

    @Override
    public AddressResponse addAddress(Long merchantId, AddressRequest request) {
        // TODO: Implement logic (validate merchant, handle isPrimary flag reset, save address)
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMerchantAddresses(Long merchantId) {
        // TODO: Implement logic (fetch all addresses for merchant)
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {
        // TODO: Implement logic (fetch address by id)
        return null;
    }

    @Override
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        // TODO: Implement logic (update address details)
        return null;
    }

    @Override
    public void deleteAddress(Long addressId) {
        // TODO: Implement logic (delete address)
    }
}
