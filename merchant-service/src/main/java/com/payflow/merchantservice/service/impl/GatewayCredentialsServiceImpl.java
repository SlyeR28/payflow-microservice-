package com.payflow.merchantservice.service.impl;

import com.payflow.merchantservice.mapper.GatewayCredentialsMapper;
import com.payflow.merchantservice.model.enums.GatewayProvider;
import com.payflow.merchantservice.payload.requestDto.AddGatewayCredentialsRequest;
import com.payflow.merchantservice.payload.responseDto.GatewayCredentialsResponse;
import com.payflow.merchantservice.repository.GatewayCredentialsRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.GatewayCredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GatewayCredentialsServiceImpl implements GatewayCredentialsService {

    private final MerchantRepository merchantRepository;
    private final GatewayCredentialsRepository gatewayCredentialsRepository;
    private final GatewayCredentialsMapper gatewayCredentialsMapper;

    @Override
    public GatewayCredentialsResponse addOrUpdateCredentials(Long merchantId, AddGatewayCredentialsRequest request) {
        // TODO: Implement logic (live sandbox pre-flight handshake, AES encrypt keys, save credentials, publish gateway event)
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatewayCredentialsResponse> getGatewayCredentials(Long merchantId) {
        // TODO: Implement logic (fetch all configured gateways with masked API keys)
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public GatewayCredentialsResponse getGatewayCredentialsByType(Long merchantId, GatewayProvider gatewayType) {
        // TODO: Implement logic (fetch gateway credentials by type)
        return null;
    }

    @Override
    public void deactivateGateway(Long merchantId, GatewayProvider gatewayType) {
        // TODO: Implement logic (toggle isActive = false)
    }

    @Override
    public void deleteGateway(Long merchantId, GatewayProvider gatewayType) {
        // TODO: Implement logic (delete gateway credentials)
    }
}
