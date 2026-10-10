package com.payflow.merchantservice.service.impl;

import com.payflow.merchantservice.exceptions.GatewayCredentialsNotFoundException;
import com.payflow.merchantservice.exceptions.InvalidGatewayCredentialsException;
import com.payflow.merchantservice.exceptions.InvalidMerchantStateException;
import com.payflow.merchantservice.exceptions.MerchantNotFoundException;
import com.payflow.merchantservice.mapper.GatewayCredentialsMapper;
import com.payflow.merchantservice.model.entity.GatewayCredentials;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.enums.GatewayProvider;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.AddGatewayCredentialsRequest;
import com.payflow.merchantservice.payload.responseDto.GatewayCredentialsResponse;
import com.payflow.merchantservice.payload.responseDto.InternalGatewayCredentialResponse;
import com.payflow.merchantservice.repository.GatewayCredentialsRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.GatewayCredentialsService;
import com.payflow.merchantservice.utils.EncryptionUtil;
import com.payflow.merchantservice.utils.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GatewayCredentialsServiceImpl implements GatewayCredentialsService {

    private final MerchantRepository merchantRepository;
    private final GatewayCredentialsRepository gatewayCredentialsRepository;
    private final GatewayCredentialsMapper gatewayCredentialsMapper;
    private final EncryptionUtil encryptionUtil;

    @Override
    public GatewayCredentialsResponse addOrUpdateCredentials(Long merchantId, AddGatewayCredentialsRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        if (merchant.getStatus() == MerchantStatus.SUSPENDED || merchant.getStatus() == MerchantStatus.REJECTED) {
            throw new InvalidMerchantStateException("Merchant in status " + merchant.getStatus() + " cannot configure gateway credentials");
        }

        validateCredentials(request.getGatewayType(), request.getApiKey(), request.getApiSecret());

        GatewayCredentials credentials = gatewayCredentialsRepository
                .findByMerchantIdAndGatewayType(merchantId, request.getGatewayType())
                .orElse(null);

        if (credentials == null) {
            credentials = GatewayCredentials.builder()
                    .merchant(merchant)
                    .gatewayType(request.getGatewayType())
                    .build();
        }

        credentials.setApiKeyEncrypted(encryptionUtil.encrypt(request.getApiKey().trim()));
        credentials.setApiSecretEncrypted(encryptionUtil.encrypt(request.getApiSecret().trim()));
        if (request.getWebhookSecret() != null && !request.getWebhookSecret().isBlank()) {
            credentials.setWebhookSecretEncrypted(encryptionUtil.encrypt(request.getWebhookSecret().trim()));
        } else {
            credentials.setWebhookSecretEncrypted(null);
        }

        credentials.setIsActive(true);
        credentials.setIsTestMode(request.getIsTestMode() != null ? request.getIsTestMode() : true);
        credentials.setVerifiedAt(Instant.now());

        GatewayCredentials saved = gatewayCredentialsRepository.save(credentials);
        log.info("Saved credentials for gateway: {} on merchant: {}", request.getGatewayType(), merchantId);

        GatewayCredentialsResponse response = gatewayCredentialsMapper.toResponse(saved);
        response.setMaskedApiKey(MaskingUtil.maskApiKey(request.getApiKey().trim()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatewayCredentialsResponse> getGatewayCredentials(Long merchantId) {
        merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        return gatewayCredentialsRepository.findByMerchantId(merchantId).stream()
                .map(this::mapWithMaskedKey)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GatewayCredentialsResponse getGatewayCredentialsByType(Long merchantId, GatewayProvider gatewayType) {
        merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        GatewayCredentials credentials = gatewayCredentialsRepository
                .findByMerchantIdAndGatewayType(merchantId, gatewayType)
                .orElseThrow(() -> new GatewayCredentialsNotFoundException("Credentials not found for gateway: " + gatewayType));

        return mapWithMaskedKey(credentials);
    }

    @Override
    public void deactivateGateway(Long merchantId, GatewayProvider gatewayType) {
        merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        GatewayCredentials credentials = gatewayCredentialsRepository
                .findByMerchantIdAndGatewayType(merchantId, gatewayType)
                .orElseThrow(() -> new GatewayCredentialsNotFoundException("Credentials not found for gateway: " + gatewayType));

        credentials.setIsActive(false);
        gatewayCredentialsRepository.save(credentials);
        log.info("Deactivated gateway: {} for merchant: {}", gatewayType, merchantId);
    }

    @Override
    public void deleteGateway(Long merchantId, GatewayProvider gatewayType) {
        merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        GatewayCredentials credentials = gatewayCredentialsRepository
                .findByMerchantIdAndGatewayType(merchantId, gatewayType)
                .orElseThrow(() -> new GatewayCredentialsNotFoundException("Credentials not found for gateway: " + gatewayType));

        gatewayCredentialsRepository.delete(credentials);
        log.info("Deleted gateway credentials: {} for merchant: {}", gatewayType, merchantId);
    }

    @Override
    public InternalGatewayCredentialResponse getInternalGatewayCredentials(Long merchantId, GatewayProvider gatewayType) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException("Merchant Not Found with Given Id: " + merchantId)
        );

        GatewayCredentials gatewayCredentials = gatewayCredentialsRepository.findByMerchantIdAndGatewayType(merchantId, gatewayType)
                .orElseThrow(() ->
                        new GatewayCredentialsNotFoundException("Credential Not Found for Gateway: " + gatewayType));

        if (Boolean.TRUE.equals(gatewayCredentials.getIsActive())){
            throw new InvalidGatewayCredentialsException("Gateway Credentials are all ready Verified....");
        }

        String apiKey = encryptionUtil.decrypt(gatewayCredentials.getApiKeyEncrypted());
        String apiSecret = encryptionUtil.decrypt(gatewayCredentials.getApiSecretEncrypted());

        String webhookSecret = gatewayCredentials.getWebhookSecretEncrypted() != null ?
                encryptionUtil.decrypt(gatewayCredentials.getWebhookSecretEncrypted()) : null;

        return InternalGatewayCredentialResponse.builder()
                .merchantId(merchantId)
                .gatewayType(gatewayType)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .webhookSecret(webhookSecret)
                .isTestMode(gatewayCredentials.getIsTestMode())
                .isActive(gatewayCredentials.getIsActive())
                .build();

    }

    private GatewayCredentialsResponse mapWithMaskedKey(GatewayCredentials creds) {
        GatewayCredentialsResponse response = gatewayCredentialsMapper.toResponse(creds);
        try {
            String rawApiKey = encryptionUtil.decrypt(creds.getApiKeyEncrypted());
            response.setMaskedApiKey(MaskingUtil.maskApiKey(rawApiKey));
        } catch (Exception e) {
            log.warn("Failed to decrypt API key for gateway credential id: {}", creds.getId());
            response.setMaskedApiKey("••••••••");
        }
        return response;
    }

    private void validateCredentials(GatewayProvider provider, String apiKey, String apiSecret) {
        if (apiKey == null || apiKey.trim().length() < 6 || apiSecret == null || apiSecret.trim().length() < 6) {
            throw new InvalidGatewayCredentialsException("API Key and Secret must be at least 6 characters long.");
        }
        if (provider == GatewayProvider.RAZORPAY) {
            String key = apiKey.trim();
            if (!key.startsWith("rzp_test_") && !key.startsWith("rzp_live_")) {
                log.warn("Razorpay API key does not match standard rzp_test_ or rzp_live_ prefix: {}", key);
            }
        }
    }
}
