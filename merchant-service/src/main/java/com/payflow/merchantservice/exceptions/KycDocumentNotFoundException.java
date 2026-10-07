package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class KycDocumentNotFoundException extends BaseException {

    public KycDocumentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "KYC_DOCUMENT_NOT_FOUND");
    }

    public KycDocumentNotFoundException(Long documentId) {
        super("KYC document not found with id: " + documentId, HttpStatus.NOT_FOUND, "KYC_DOCUMENT_NOT_FOUND");
    }
}
