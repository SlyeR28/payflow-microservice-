package com.payflow.merchantservice.service;

import com.payflow.merchantservice.model.enums.GatewayProvider;
import com.payflow.merchantservice.payload.requestDto.AddGatewayCredentialsRequest;
import com.payflow.merchantservice.payload.responseDto.GatewayCredentialsResponse;
import com.payflow.merchantservice.payload.responseDto.InternalGatewayCredentialResponse;

import java.util.List;

public interface GatewayCredentialsService {

    GatewayCredentialsResponse addOrUpdateCredentials(Long merchantId, AddGatewayCredentialsRequest request);

    List<GatewayCredentialsResponse> getGatewayCredentials(Long merchantId);

    GatewayCredentialsResponse getGatewayCredentialsByType(Long merchantId, GatewayProvider gatewayType);

    void deactivateGateway(Long merchantId, GatewayProvider gatewayType);

    void deleteGateway(Long merchantId, GatewayProvider gatewayType);

    InternalGatewayCredentialResponse getInternalGatewayCredentials(Long merchantId, GatewayProvider gatewayType);
}
