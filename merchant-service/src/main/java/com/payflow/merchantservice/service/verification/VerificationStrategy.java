package com.payflow.merchantservice.service.verification;

import com.payflow.merchantservice.service.verification.dto.VerificationContext;
import com.payflow.merchantservice.service.verification.dto.VerificationResult;

public interface VerificationStrategy {

    VerificationType getVerificationType();

    void validatePreConditions(VerificationContext context);

    VerificationResult execute(VerificationContext context);

}
