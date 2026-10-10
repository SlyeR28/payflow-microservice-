package com.payflow.merchantservice.service.verification.impl;

import com.payflow.common.exceptions.BusinessException;
import com.payflow.merchantservice.model.enums.BusinessType;
import com.payflow.merchantservice.service.verification.VerificationStrategy;
import com.payflow.merchantservice.service.verification.VerificationType;
import com.payflow.merchantservice.service.verification.dto.PanVerificationResult;
import com.payflow.merchantservice.service.verification.dto.VerificationContext;
import com.payflow.merchantservice.service.verification.dto.VerificationResult;
import com.payflow.merchantservice.utils.FuzzyMatchUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PanVerificationStrategy implements VerificationStrategy {

    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    @Override
    public VerificationType getVerificationType() {
        return VerificationType.PAN;
    }

    @Override
    public void validatePreConditions(VerificationContext context) {
        if (context.getPanNumber() == null || context.getPanNumber().isBlank()){
            throw new BusinessException("PAN number is required for verification", "PAN_REQUIRED");
        }

        String normalizedPan = context.getPanNumber().trim().toUpperCase();
        if (!PAN_PATTERN.matcher(normalizedPan).matches()){
            throw new BusinessException("Invalid PAN format: " + normalizedPan, "INVALID_PAN_FORMAT");
        }

        if (context.getLegalName() == null || context.getLegalName().isBlank()){
            throw new BusinessException("Legal name is required to verify against PAN records" , "LEGAL_NAME_REQUIRED");
        }

        //Validate 4 th character against Business Type if businessType is Provided
        char entityTypeChar = normalizedPan.charAt(3);
        if (context.getBusinessType() != null) {
             validatePanCategoryWithBusinessType(entityTypeChar, context.getBusinessType());
        }

    }

    @Override
    public VerificationResult execute(VerificationContext context) {
        String pan = context.getPanNumber().trim().toUpperCase();
        log.info("Executing PAN verification for merchantId={}, PAN ending with={}",
                context.getMerchantId(), pan.substring(Math.max(0, pan.length() - 4)));

        //Simulated Tax Authority /NSDL Lookup (In prod, call external RestCLient Api)
        char categoryChar = pan.charAt(3);
        String panCategory = resolvePanCategory(categoryChar);


        if (pan.startsWith("FAIL")){
            return PanVerificationResult.builder()
                    .verificationType(VerificationType.PAN)
                    .panNumber(pan)
                    .panStatus("INVALID")
                    .panCategory(panCategory)
                    .successful(false)
                    .nameMatched(false)
                    .nameMatchScore(0.0)
                    .failureReason("PAN record not found in Income Tax Department records")
                    .executedAt(Instant.now())
                    .build();
        }

        // Deterministic simulated name from tax records (uses context legalName , or simulated name)
        String registeredNameOnPan = context.getLegalName().trim().toUpperCase();
        if (context.getMetadata() != null && context.getMetadata().containsKey("mockTaxName")){
            registeredNameOnPan = context.getMetadata().get("mockTaxName").toString();
        }

        // run Jaro-Winkler fuzzy Match
        double similarityScore = FuzzyMatchUtil
                       .calculateSimilarity(registeredNameOnPan, context.getLegalName());

        double threshold = context
                       .getNameMatchThreshold() != null ? context.getNameMatchThreshold() : 0.70;
         boolean nameMatched =  similarityScore>=threshold;

         log.info("PAN match evaluated: merchantName='{}', panName='{}', score={}, threshold={}, matched={}," ,
                 context.getLegalName(), registeredNameOnPan, similarityScore, threshold, nameMatched);

         return PanVerificationResult.builder()
                 .verificationType(VerificationType.PAN)
                 .panNumber(pan)
                 .panStatus("EXISTING_AND_VALIDATED")
                 .panCategory(panCategory)
                 .registeredNameOnPan(registeredNameOnPan)
                 .registeredName(registeredNameOnPan)
                 .nameMatchScore(similarityScore)
                 .nameMatched(nameMatched)
                 .successful(nameMatched)
                 .failureReason(nameMatched ? null : "Name on PAN (" + registeredNameOnPan + ") did not match registered legal name")
                 .dateOfIncorporationOrBirth(LocalDate.of(2018, 1, 15))
                 .aadhaarSeeded(true)
                 .referenceId("NSDL-" + System.currentTimeMillis())
                 .rawResponse(Map.of("status", "SUCCESS", "taxOffice", "WARD_1_DELHI"))
                 .executedAt(Instant.now())
                 .build();



    }


    private void validatePanCategoryWithBusinessType(char panCategoryChar , BusinessType businessType){

        switch (businessType){
            case INDIVIDUAL , PROPRIETORSHIP-> {
                if (panCategoryChar != 'P'){
                    throw new BusinessException("Invalid PAN category for business type: " + businessType, "INVALID_PAN_CATEGORY");
                }
            }
            case PRIVATE_LIMITED , PUBLIC_LIMITED  -> {
                if (panCategoryChar != 'C'){
                    throw new BusinessException("Invalid PAN category for business type: " + businessType, "INVALID_PAN_CATEGORY");
                }
            }
            case PARTNERSHIP , LLP ->{
                if (panCategoryChar != 'F'){
                    throw new BusinessException("Invalid PAN category for business type: " + businessType, "INVALID_PAN_CATEGORY");
                }
            }
            case TRUST_NGO -> {
                if (panCategoryChar != 'T'){
                    throw new BusinessException("Invalid PAN category for business type: " + businessType, "INVALID_PAN_CATEGORY");
                }
            }
        }

    }


    private String resolvePanCategory(char c){
        return switch (c){
            case 'P' -> "INDIVIDUAL";
            case 'C' -> "COMPANY";
            case 'F' -> "FIRM";
            case 'T' -> "TRUST";
            case 'H' -> "HUF";
            default -> "OTHER";
        };
    }

}

