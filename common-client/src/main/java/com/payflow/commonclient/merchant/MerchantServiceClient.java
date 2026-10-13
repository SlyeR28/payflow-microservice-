package com.payflow.commonclient.merchant;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.merchant.dto.MerchantClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "merchant-service" , fallback = MerchantServiceFallBack.class)
public interface MerchantServiceClient {

    @GetMapping("/api/v1/merchants/{id}")
   ApiResponse<MerchantClientResponse> getMerchantById(@PathVariable("id") Long id);

}
