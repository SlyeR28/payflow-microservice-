package com.payflow.merchantservice.payload.responseDto;

import com.payflow.merchantservice.model.enums.AddressType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {

    private Long id;
    private AddressType addressType;
    private String label;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isPrimary;
    private Instant createdAt;
    private Instant updatedAt;
}
