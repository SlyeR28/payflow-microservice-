package com.payflow.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.notificationservice.dto.request.CreateTemplateRequest;
import com.payflow.notificationservice.dto.request.SendNotificationRequest;
import com.payflow.notificationservice.dto.request.UpdatePreferenceRequest;
import com.payflow.notificationservice.dto.response.NotificationResponse;
import com.payflow.notificationservice.dto.response.PreferenceResponse;
import com.payflow.notificationservice.dto.response.TemplateResponse;
import com.payflow.notificationservice.exception.NotificationGlobalExceptionHandler;
import com.payflow.notificationservice.exception.NotificationNotFoundException;
import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.NotificationStatus;
import com.payflow.notificationservice.model.enums.TemplateType;
import com.payflow.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new NotificationGlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/notifications/send: Should return 201 Created on valid dispatch")
    void sendNotification_Success() throws Exception {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .idempotencyKey("idemp-controller-01")
                .recipient("test@example.com")
                .channel(NotificationChannel.EMAIL)
                .templateType(TemplateType.PAYMENT_SUCCESS)
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .notificationReference("NOTIF-REF-100")
                .recipient("test@example.com")
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .build();

        when(notificationService.sendNotification(any(SendNotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationReference").value("NOTIF-REF-100"))
                .andExpect(jsonPath("$.data.status").value("SENT"));

        verify(notificationService).sendNotification(any(SendNotificationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/send: Should return 400 Bad Request on missing fields")
    void sendNotification_MissingFields_ReturnsBadRequest() throws Exception {
        SendNotificationRequest invalidRequest = SendNotificationRequest.builder()
                .idempotencyKey("") // blank
                .recipient(null) // null
                .build();

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{reference}: Should return 200 OK")
    void getNotification_Success() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .notificationReference("NOTIF-REF-200")
                .recipient("user@example.com")
                .status(NotificationStatus.SENT)
                .build();

        when(notificationService.getNotificationByReference("NOTIF-REF-200")).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications/NOTIF-REF-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationReference").value("NOTIF-REF-200"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{reference}: Should return 404 Not Found")
    void getNotification_NotFound() throws Exception {
        when(notificationService.getNotificationByReference("NOTIF-MISSING"))
                .thenThrow(new NotificationNotFoundException("NOTIF-MISSING"));

        mockMvc.perform(get("/api/v1/notifications/NOTIF-MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/templates: Should return 201 Created")
    void createTemplate_Success() throws Exception {
        CreateTemplateRequest request = CreateTemplateRequest.builder()
                .templateCode("PAYMENT_SUCCESS_EMAIL")
                .templateType(TemplateType.PAYMENT_SUCCESS)
                .channel(NotificationChannel.EMAIL)
                .bodyTemplate("Dear Customer, your payment is received.")
                .build();

        TemplateResponse response = TemplateResponse.builder()
                .templateCode("PAYMENT_SUCCESS_EMAIL")
                .channel(NotificationChannel.EMAIL)
                .bodyTemplate("Dear Customer, your payment is received.")
                .active(true)
                .build();

        when(notificationService.createTemplate(any(CreateTemplateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.templateCode").value("PAYMENT_SUCCESS_EMAIL"));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/preferences/{merchantId}: Should return 200 OK")
    void updatePreference_Success() throws Exception {
        UpdatePreferenceRequest request = UpdatePreferenceRequest.builder()
                .emailEnabled(true)
                .smsEnabled(true)
                .build();

        PreferenceResponse response = PreferenceResponse.builder()
                .merchantId(101L)
                .emailEnabled(true)
                .smsEnabled(true)
                .build();

        when(notificationService.updatePreference(eq(101L), any(UpdatePreferenceRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/notifications/preferences/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.merchantId").value(101));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/preferences/{merchantId}: Should return 200 OK")
    void getPreference_Success() throws Exception {
        PreferenceResponse response = PreferenceResponse.builder()
                .merchantId(101L)
                .emailEnabled(true)
                .smsEnabled(false)
                .build();

        when(notificationService.getPreference(101L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications/preferences/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.merchantId").value(101));
    }
}
