package com.payflow.commonclient.merchant;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.merchant.dto.MerchantClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MerchantServiceFallBack implements MerchantServiceClient{


    @Override
    public ApiResponse<MerchantClientResponse> getMerchantById(Long id) {
        log.error("Merchant Service Client fallback triggered for merchantId: {}" , id);
        return ApiResponse.<MerchantClientResponse>builder()
                .success(false)
                .message("Merchant Service is temporarily unreachable")
                .build();
    }
}
