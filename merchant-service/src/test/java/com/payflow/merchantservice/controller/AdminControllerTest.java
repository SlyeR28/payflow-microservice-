package com.payflow.merchantservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payflow.common.dto.PagedResponse;
import com.payflow.common.handler.GlobalExceptionHandler;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.RejectMerchantRequest;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import com.payflow.merchantservice.security.service.SecurityUtil;
import com.payflow.merchantservice.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private MerchantService merchantService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private AdminController adminController;

    private MerchantResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = MerchantResponse.builder()
                .id(1L)
                .userId(101L)
                .businessName("Acme Corp")
                .legalName("Acme Corporation Pvt Ltd")
                .status(MerchantStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Verify class-level @PreAuthorize hasRole('ADMIN') exists and NO methods are annotated with @PreAuthorize")
    void testSecurityAnnotationsAreAtClassLevelOnly() {
        PreAuthorize classAnnotation = AdminController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(classAnnotation, "AdminController must be annotated with @PreAuthorize at class level");
        assertEquals("hasRole('ADMIN')", classAnnotation.value(), "Class-level annotation must require ADMIN role");

        for (Method method : AdminController.class.getDeclaredMethods()) {
            PreAuthorize methodAnnotation = method.getAnnotation(PreAuthorize.class);
            assertNull(methodAnnotation, "Method " + method.getName() + " must NOT have @PreAuthorize; access must be class-level only");
        }
    }

    @Test
    @DisplayName("GET /api/v1/merchants - List all merchants")
    void testGetAllMerchants() throws Exception {
        PagedResponse<MerchantResponse> pagedResponse = PagedResponse.<MerchantResponse>builder()
                .content(List.of(sampleResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(merchantService.getAllMerchants(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].businessName").value("Acme Corp"));

        verify(merchantService).getAllMerchants(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/admin/merchants?status=ACTIVE - List merchants by status query param")
    void testGetAllMerchantsWithStatusFilter() throws Exception {
        PagedResponse<MerchantResponse> pagedResponse = PagedResponse.<MerchantResponse>builder()
                .content(List.of(sampleResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(merchantService.listAllByStatus(eq(MerchantStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/merchants")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));

        verify(merchantService).listAllByStatus(eq(MerchantStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/merchants/status/ACTIVE - List merchants by path status")
    void testGetMerchantsByStatus() throws Exception {
        PagedResponse<MerchantResponse> pagedResponse = PagedResponse.<MerchantResponse>builder()
                .content(List.of(sampleResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(merchantService.listAllByStatus(eq(MerchantStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/merchants/status/ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].businessName").value("Acme Corp"));
    }

    @Test
    @DisplayName("GET /api/v1/merchants/{id} - Get merchant by ID")
    void testGetMerchantById() throws Exception {
        when(merchantService.getMerchantById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/merchants/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.businessName").value("Acme Corp"));
    }

    @Test
    @DisplayName("POST & PUT /api/v1/merchants/{id}/approve - Approve merchant")
    void testApproveMerchant() throws Exception {
        when(securityUtil.getCurrentUserId()).thenReturn(999L);
        when(merchantService.approve(1L, 999L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/merchants/1/approve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Merchant approved successfully"))
                .andExpect(jsonPath("$.data.id").value(1L));

        mockMvc.perform(put("/api/v1/admin/merchants/1/approve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/merchants/{id}/reject - Reject merchant with body")
    void testRejectMerchantWithBody() throws Exception {
        RejectMerchantRequest request = RejectMerchantRequest.builder()
                .reason("Fraudulent documents")
                .build();

        sampleResponse.setStatus(MerchantStatus.REJECTED);
        sampleResponse.setRejectReason("Fraudulent documents");

        when(securityUtil.getCurrentUserId()).thenReturn(999L);
        when(merchantService.reject(1L, 999L, "Fraudulent documents")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/merchants/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Merchant rejected successfully"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("PUT /api/v1/merchants/{id}/reject - Reject merchant with query param")
    void testRejectMerchantWithQueryParam() throws Exception {
        sampleResponse.setStatus(MerchantStatus.REJECTED);
        sampleResponse.setRejectReason("KYC incomplete");

        when(securityUtil.getCurrentUserId()).thenReturn(999L);
        when(merchantService.reject(1L, 999L, "KYC incomplete")).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/merchants/1/reject")
                        .param("reason", "KYC incomplete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Merchant rejected successfully"));
    }

    @Test
    @DisplayName("POST & PUT /api/v1/merchants/{id}/suspend - Suspend merchant")
    void testSuspendMerchant() throws Exception {
        sampleResponse.setStatus(MerchantStatus.SUSPENDED);

        when(securityUtil.getCurrentUserId()).thenReturn(999L);
        when(merchantService.suspend(1L, 999L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/merchants/1/suspend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Merchant suspended successfully"))
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

        mockMvc.perform(put("/api/v1/admin/merchants/1/suspend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
