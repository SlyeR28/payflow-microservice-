package com.payflow.merchantservice.service.impl;

import com.payflow.common.dto.PagedResponse;
import com.payflow.merchantservice.exceptions.InvalidMerchantStateException;
import com.payflow.merchantservice.exceptions.MerchantAlreadyExistsException;
import com.payflow.merchantservice.exceptions.MerchantNotFoundException;
import com.payflow.merchantservice.mapper.MerchantAddressMapper;
import com.payflow.merchantservice.mapper.MerchantMapper;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.entity.MerchantAddress;
import com.payflow.merchantservice.model.entity.MerchantKyc;
import com.payflow.merchantservice.model.enums.AddressType;
import com.payflow.merchantservice.model.enums.KycStatus;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.CreateMerchantRequest;
import com.payflow.merchantservice.payload.requestDto.UpdateMerchantRequest;
import com.payflow.merchantservice.payload.responseDto.AddressResponse;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import com.payflow.merchantservice.repository.MerchantAddressRepository;
import com.payflow.merchantservice.repository.MerchantKycRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.security.service.SecurityUtil;
import com.payflow.merchantservice.service.MerchantService;
import com.payflow.merchantservice.utils.EncryptionUtil;
import com.payflow.merchantservice.utils.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final EncryptionUtil encryptionUtil;
    private final SecurityUtil securityUtil;
    private final MerchantKycRepository merchantKycRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final MerchantAddressMapper merchantAddressMapper;


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public MerchantResponse createMerchant(Long userId, CreateMerchantRequest request) {
        // TODO: Implement logic (validation, encryption of PAN, duplicate check, save entity)
        // if user already exists with merchant role
        if(merchantRepository.existsByUserId(userId)){
            throw new MerchantAlreadyExistsException(userId);
        }
        // if business email already exists
        if (merchantRepository.existsByBusinessEmail(request.getBusinessEmail())){
            throw new MerchantAlreadyExistsException(request.getBusinessEmail());
        }

       Merchant entity = Merchant.builder()
               .userId(userId)
               .businessName(request.getBusinessName())
               .legalName(request.getLegalName())
               .businessEmail(request.getBusinessEmail())
               .businessPhone(request.getBusinessPhone())
               .website(request.getWebsite())
               .businessCategory(request.getBusinessCategory())
               .businessType(request.getBusinessType())
               .gstin(request.getGstin())
               .panNumberEncrypted(encryptionUtil.encrypt(request.getPanNumber()))
               .panNumberMasked(MaskingUtil.maskPan(request.getPanNumber()))
               .status(MerchantStatus.PENDING)
               .build();
         merchantRepository.save(entity);

        // initial address as registered
        MerchantAddress merchantAddress = MerchantAddress.builder()
                .merchant(entity)
                .addressType(request.getAddressRequest().getAddressType() != null
                        ? request.getAddressRequest().getAddressType()
                        : AddressType.REGISTERED) // initial registered address only
                .label(request.getAddressRequest().getLabel() != null
                        ? request.getAddressRequest().getLabel()
                        : "Registered Office")
                .addressLine1(request.getAddressRequest().getAddressLine1())
                .addressLine2(request.getAddressRequest().getAddressLine2())
                .city(request.getAddressRequest().getCity())
                .state(request.getAddressRequest().getState())
                .postalCode(request.getAddressRequest().getPostalCode())
                .country(request.getAddressRequest().getCountry())
                .isPrimary(request.getAddressRequest().getIsPrimary() == null
                        || request.getAddressRequest().getIsPrimary())
                .build();

        merchantAddressRepository.save(merchantAddress);
        entity.getAddresses().add(merchantAddress);

        // initialize the kyc record
        merchantKycRepository.save(
                MerchantKyc.builder()
                        .merchant(entity)
                        .status(KycStatus.PENDING)
                        .build()
        );

        log.info("Merchant created: merchantId={} userId={}", entity.getId(), userId);
        // mail / notification // for verification
        return toResponseWithAddresses(entity);
    }


    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchantByUserId(Long userId) {
        boolean b = securityUtil.hasRole("MERCHANT");
        if (!b) {
            throw new MerchantNotFoundException(userId);
        }
        return toResponseWithAddresses(merchantRepository
                .findByUserId(userId).orElseThrow(() -> new MerchantNotFoundException(userId)));
    }


    @Override
    public MerchantResponse updateMerchant(Long userId, UpdateMerchantRequest request) {
        // find by merchant id
        Merchant merchant = merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new MerchantNotFoundException(userId));

        if (merchant.getStatus() == MerchantStatus.SUSPENDED){
            throw new InvalidMerchantStateException("Suspended merchants cannot update profile");
        }

        if (merchant.getStatus() == MerchantStatus.REJECTED){
            throw new InvalidMerchantStateException("Rejected merchants cannot update profile");
        }

        if (request.getBusinessName()  != null) merchant.setBusinessName(request.getBusinessName());
        if (request.getLegalName()     != null) merchant.setLegalName(request.getLegalName());
        if (request.getBusinessPhone() != null) merchant.setBusinessPhone(request.getBusinessPhone());
        if (request.getWebsite()       != null) merchant.setWebsite(request.getWebsite());

        return merchantMapper.toResponse(merchantRepository.save(merchant));
    }


    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchantById(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
        return toResponseWithAddresses(merchant);
    }



    @Override
    public MerchantResponse approve(Long merchantId, Long adminId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        if (merchant.getStatus() == MerchantStatus.ACTIVE){
            throw new InvalidMerchantStateException("Active merchants cannot be approved");
        }

        merchant.setStatus(MerchantStatus.ACTIVE);
        merchant.setApprovedBy(adminId);
        merchant.setApprovedAt(Instant.now());
        merchant.setRejectReason(null);
        merchantRepository.save(merchant);

        log.info("Merchant approved: merchantId={} adminUserId={}", merchantId, adminId);


        return merchantMapper.toResponse(merchant);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MerchantResponse> listAllByStatus(MerchantStatus status, Pageable pageable) {
        Page<Merchant> merchantPage = merchantRepository.findByStatus(status, pageable);
        return PagedResponse.from(merchantPage, merchantMapper::toResponse);
    }

    @Override
    public MerchantResponse reject(Long merchantId, Long adminId, String reason) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        merchant.setStatus(MerchantStatus.REJECTED);
        merchant.setApprovedBy(adminId);
        merchant.setRejectReason(reason);
        merchantRepository.save(merchant);

        log.info("Merchant rejected: merchantId={} adminUserId={}", merchantId, adminId);


        return merchantMapper.toResponse(merchant);
    }

    @Override
    public MerchantResponse suspend(Long merchantId, Long adminId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        if (merchant.getStatus() == MerchantStatus.SUSPENDED){
            throw new InvalidMerchantStateException("Suspended merchants cannot be suspended");
        }

        merchant.setStatus(MerchantStatus.SUSPENDED);
        merchant.setApprovedBy(adminId);
        merchantRepository.save(merchant);

        log.info("Merchant suspended: merchantId={} adminUserId={}", merchantId, adminId);


        return merchantMapper.toResponse(merchant);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MerchantResponse> getAllMerchants(Pageable pageable) {
        Page<Merchant> merchantPage = merchantRepository.findAll(pageable);
        return PagedResponse.from(merchantPage, merchantMapper::toResponse);
    }



    // helper
    private MerchantResponse toResponseWithAddresses(Merchant merchant) {
        MerchantResponse response = merchantMapper.toResponse(merchant);
        List<MerchantAddress> merchantAddresses = merchantAddressRepository
                .findByMerchantId(merchant.getId());
        if (merchantAddresses.isEmpty() && merchant.getAddresses() != null && !merchant.getAddresses().isEmpty()) {
            merchantAddresses = merchant.getAddresses();
        }
        List<AddressResponse> addresses = merchantAddresses
                .stream()
                .map(merchantAddressMapper::toResponse)
                .toList();
        response.setAddresses(addresses);
        return response;
    }
}
