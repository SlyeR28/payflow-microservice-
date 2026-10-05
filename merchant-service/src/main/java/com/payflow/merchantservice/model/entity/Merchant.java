package com.payflow.merchantservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchant" , indexes = {
        @Index(name = "idx_merchants_userId" , columnList = "userId" , unique = true),
        @Index(name = "idx_merchants_business_email" , columnList = "businessEmail"),
        @Index(name = "idx_merchants_status" , columnList = "status")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId" , nullable = false , unique = true)
    private Long userId;

    @Column(name = "businessName" , nullable = false , length = 200)
    private String businessName;

    @Column(name = "legalName" , nullable = false , length = 255)
    private String legalName;

    @Column(name = "businessEmail" , nullable = false , length = 255)
    private String businessEmail;

    @Column(name = "businessPhone" , length = 20)
    private String businessPhone;

    @Column(name = "website")
    private String website;










}
