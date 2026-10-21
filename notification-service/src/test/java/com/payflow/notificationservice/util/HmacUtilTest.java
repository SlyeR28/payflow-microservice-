package com.payflow.notificationservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacUtilTest {

    @Test
    @DisplayName("calculateHmac: Should produce deterministic 64-character lowercase hex digest")
    void calculateHmac_Success() {
        String data = "{\"event\":\"payment.captured\",\"amount\":5000}";
        String secret = "webhook_secret_key_12345";

        String signature1 = HmacUtil.calculateHmac(data, secret);
        String signature2 = HmacUtil.calculateHmac(data, secret);

        assertThat(signature1).isNotNull();
        assertThat(signature1).hasSize(64);
        assertThat(signature1).isEqualTo(signature2);
        assertThat(signature1).matches("^[a-f0-9]{64}$");
    }

    @Test
    @DisplayName("calculateHmac: Different payload produces different signature")
    void calculateHmac_DifferentPayload_ProducesDifferentSignature() {
        String secret = "shared_secret";
        String sig1 = HmacUtil.calculateHmac("payload-1", secret);
        String sig2 = HmacUtil.calculateHmac("payload-2", secret);

        assertThat(sig1).isNotEqualTo(sig2);
    }
}
