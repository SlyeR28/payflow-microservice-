package com.payflow.merchantservice.service.impl;

import com.payflow.merchantservice.exceptions.DuplicateAddressException;
import com.payflow.merchantservice.exceptions.InvalidMerchantStateException;
import com.payflow.merchantservice.exceptions.MerchantNotFoundException;
import com.payflow.merchantservice.mapper.MerchantAddressMapper;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.entity.MerchantAddress;
import com.payflow.merchantservice.model.enums.AddressType;
import com.payflow.merchantservice.payload.requestDto.AddressRequest;
import com.payflow.merchantservice.payload.responseDto.AddressResponse;
import com.payflow.merchantservice.repository.MerchantAddressRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.MerchantAddressService;
import com.payflow.merchantservice.utils.AddressHashUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MerchantAddressServiceImpl implements MerchantAddressService {

    private final MerchantRepository merchantRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final MerchantAddressMapper merchantAddressMapper;



    private static final int MAX_ADDRESS_PER_MERCHANT = 5;
    private static final int SYNDICATE_FRAUD_CHECK_LIMIT = 2;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public AddressResponse addAddress(Long merchantId, AddressRequest request) {

        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(
                () -> new MerchantNotFoundException(merchantId)
        );

        // 1. quota check that merchant has crossed limit or not
        List<MerchantAddress> currentAddress = merchantAddressRepository.findByMerchantId(merchantId);
        if (currentAddress.size() >= MAX_ADDRESS_PER_MERCHANT) {
            throw new InvalidMerchantStateException("Merchant has reached the maximum number of addresses");
        }

        // 2. hash the address
        String addressHash =  AddressHashUtility.computeHash(request);

        // 3. check if address already exists
        Optional<MerchantAddress> addressHash1 = merchantAddressRepository
                .findByMerchantIdAndAddressHash(merchantId, addressHash);

        if(addressHash1.isPresent()){
            MerchantAddress merchantAddress = addressHash1.get();
            throw new DuplicateAddressException(
                    String.format(
                            "This physical address is already exists on your Profile under type '%s' (label: '%s').",
                            merchantAddress.getAddressType(),
                            merchantAddress.getLabel() != null ? merchantAddress.getLabel() : "N/A"
                    ));
        }

        // 4 AddressType business rule : Only one Register address is allowed
        if (request.getAddressType() == AddressType.REGISTERED &&
         merchantAddressRepository.findByMerchantIdAndAddressType(merchantId , AddressType.REGISTERED).isPresent()){
            throw new InvalidMerchantStateException(
                    "A Register address is already exists on your Profile. Please update the existing address.");
        }

        // 5 AML cross-Merchant syndicate fraud check that address is already defined for other merchants or not
        long globalReuseCount = merchantAddressRepository.countByAddressHash(addressHash);
        if (globalReuseCount >= SYNDICATE_FRAUD_CHECK_LIMIT) {
            log.warn("🚨 [AML-ALERT] Address hash {} is linked to {} different merchant accounts! MerchantId={}",
                    addressHash, globalReuseCount, merchantId);
            // in production publish to kafka AML alert for compilane officer review
        }

        // 6 primary flag management
        boolean makePrimary = Boolean.TRUE.equals(request.getIsPrimary()) || currentAddress.isEmpty();
        if (makePrimary) {
            merchantAddressRepository.findByMerchantIdAndIsPrimaryTrue(merchantId).ifPresent(
                    existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        merchantAddressRepository.save(existingPrimary);
                    });
        }

            // persist entity
            MerchantAddress entity = merchantAddressMapper.toEntity(request);
            entity.setMerchant(merchant);
            entity.setAddressHash(addressHash);
            entity.setIsPrimary(makePrimary);

            MerchantAddress saved = merchantAddressRepository.save(entity);
            log.info("Address created: addressId={} merchantId={} type={} hash={}",
                    saved.getId(), merchantId, saved.getAddressType(), addressHash);

            return  merchantAddressMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMerchantAddresses(Long merchantId) {
        if (!merchantRepository.existsById(merchantId)){
            throw new MerchantNotFoundException(merchantId);
        }
        return merchantAddressRepository.findByMerchantId(merchantId).stream()
                .map(merchantAddressMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {
        return merchantAddressMapper.
                toResponse(merchantAddressRepository.findById(addressId)
                        .orElseThrow(
                () -> new InvalidMerchantStateException("Address not found with id: " +addressId)
        ));
    }

    @Override
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        MerchantAddress address = merchantAddressRepository.findById(addressId)
                .orElseThrow(() -> new InvalidMerchantStateException("Address not found with id: " + addressId));

        // new hash
        String newHash = AddressHashUtility.computeHash(request);

        // Check if updating creates a duplicate with ANOTHER address of this merchant
        Optional<MerchantAddress> existingWithSameHash = merchantAddressRepository.
                                   findByMerchantIdAndAddressHash(address.getMerchant().getId(), newHash);

        if (existingWithSameHash.isPresent() && !existingWithSameHash.get().getId().equals(addressId)){
            throw new DuplicateAddressException("This physical address is already exists on your Profile");
        }

        address.setAddressType(request.getAddressType());
        address.setLabel(request.getLabel());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setAddressHash(newHash);

        if (Boolean.TRUE.equals(request.getIsPrimary()) && !Boolean.TRUE.equals(address.getIsPrimary())){
            merchantAddressRepository.findByMerchantIdAndIsPrimaryTrue(address.getMerchant().getId()).ifPresent(
                    existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        merchantAddressRepository.save(existingPrimary);
                    });
            address.setIsPrimary(true);
        }
        return merchantAddressMapper.toResponse(merchantAddressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long addressId) {
        MerchantAddress merchantAddress = merchantAddressRepository.findById(addressId).
                orElseThrow(() -> new InvalidMerchantStateException("Address not found with id: " + addressId));

        if (Boolean.TRUE.equals(merchantAddress.getIsPrimary())){
            throw new InvalidMerchantStateException("Primary address cannot be deleted. Please set another address as primary");
        }
        merchantAddressRepository.delete(merchantAddress);
        log.info("Address deleted: addressId={} merchantId={}", addressId, merchantAddress.getMerchant().getId());
    }
}
