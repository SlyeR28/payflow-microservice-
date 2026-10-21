package com.payflow.notificationservice.service;

import com.payflow.common.exceptions.BusinessException;
import com.payflow.notificationservice.dto.request.CreateTemplateRequest;
import com.payflow.notificationservice.dto.request.SendNotificationRequest;
import com.payflow.notificationservice.dto.request.UpdatePreferenceRequest;
import com.payflow.notificationservice.dto.response.NotificationResponse;
import com.payflow.notificationservice.dto.response.PreferenceResponse;
import com.payflow.notificationservice.dto.response.TemplateResponse;
import com.payflow.notificationservice.exception.NotificationNotFoundException;
import com.payflow.notificationservice.mapper.NotificationMapper;
import com.payflow.notificationservice.model.document.DeliveryAttempt;
import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.document.NotificationPreference;
import com.payflow.notificationservice.model.document.NotificationTemplate;
import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.NotificationPriority;
import com.payflow.notificationservice.model.enums.NotificationStatus;
import com.payflow.notificationservice.model.enums.TemplateType;
import com.payflow.notificationservice.provider.NotificationProvider;
import com.payflow.notificationservice.provider.ProviderRegistry;
import com.payflow.notificationservice.provider.ProviderResult;
import com.payflow.notificationservice.repository.DeliveryAttemptRepository;
import com.payflow.notificationservice.repository.NotificationMessagesRepository;
import com.payflow.notificationservice.repository.NotificationPreferenceRepository;
import com.payflow.notificationservice.repository.NotificationTemplateRepository;
import com.payflow.notificationservice.service.impl.NotificationServiceImpl;
import com.payflow.notificationservice.service.impl.TemplateEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationMessagesRepository notificationMessagesRepository;

    @Mock
    private DeliveryAttemptRepository deliveryAttemptRepository;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private ProviderRegistry providerRegistry;

    @Mock
    private NotificationProvider mockEmailProvider;

    private TemplateEngineService templateEngineService;
    private NotificationMapper notificationMapper;
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        templateEngineService = new TemplateEngineService();
        notificationMapper = Mappers.getMapper(NotificationMapper.class);
        notificationService = new NotificationServiceImpl(
                notificationMessagesRepository,
                deliveryAttemptRepository,
                notificationTemplateRepository,
                notificationPreferenceRepository,
                providerRegistry,
                templateEngineService,
                notificationMapper
        );
    }

    @Test
    @DisplayName("sendNotification: Should return cached response on idempotent hit")
    void sendNotification_IdempotencyHit_ReturnsCached() {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .idempotencyKey("idemp-hit-123")
                .recipient("user@payflow.com")
                .channel(NotificationChannel.EMAIL)
                .templateType(TemplateType.PAYMENT_SUCCESS)
                .build();

        NotificationMessages existing = NotificationMessages.builder()
                .id("doc-101")
                .notificationReference("NOTIF-CACHED-001")
                .idempotencyKey("idemp-hit-123")
                .recipient("user@payflow.com")
                .notificationChannel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .build();

        when(notificationMessagesRepository.findByIdempotencyKey("idemp-hit-123")).thenReturn(Optional.of(existing));

        NotificationResponse response = notificationService.sendNotification(request);

        assertThat(response).isNotNull();
        assertThat(response.getNotificationReference()).isEqualTo("NOTIF-CACHED-001");
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);

        verify(providerRegistry, never()).getProvider(any());
        verify(deliveryAttemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendNotification: Should throw BusinessException when channel is disabled for merchant")
    void sendNotification_ChannelDisabledByMerchant_ThrowsException() {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .idempotencyKey("idemp-pref-disabled")
                .merchantId(101L)
                .recipient("+919876543210")
                .channel(NotificationChannel.SMS)
                .templateType(TemplateType.OTP_ALERT)
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .merchantId(101L)
                .smsEnabled(false)
                .build();

        when(notificationMessagesRepository.findByIdempotencyKey("idemp-pref-disabled")).thenReturn(Optional.empty());
        when(notificationPreferenceRepository.findByMerchantId(101L)).thenReturn(Optional.of(preference));

        assertThatThrownBy(() -> notificationService.sendNotification(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disabled for this merchant");

        verify(providerRegistry, never()).getProvider(any());
    }

    @Test
    @DisplayName("sendNotification: Should render template, dispatch via provider, and record SENT status")
    void sendNotification_WithTemplate_SuccessfulDispatch() {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .idempotencyKey("idemp-send-success")
                .merchantId(101L)
                .recipient("customer@example.com")
                .channel(NotificationChannel.EMAIL)
                .templateType(TemplateType.PAYMENT_SUCCESS)
                .priority(NotificationPriority.HIGH)
                .payload(Map.of("customerName", "Bob", "amount", "₹500.00"))
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .templateCode("PAYMENT_SUCCESS-EMAIL")
                .templateType(TemplateType.PAYMENT_SUCCESS)
                .channel(NotificationChannel.EMAIL)
                .subject("Payment Confirmed: {{amount}}")
                .bodyTemplate("Hello [[${customerName}]], your payment of {{amount}} is received.")
                .active(true)
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .merchantId(101L)
                .emailEnabled(true)
                .build();

        ProviderResult providerResult = ProviderResult.success("SMTP-Provider", "smtp-msg-7788", 200, 150);

        when(notificationMessagesRepository.findByIdempotencyKey("idemp-send-success")).thenReturn(Optional.empty());
        when(notificationPreferenceRepository.findByMerchantId(101L)).thenReturn(Optional.of(preference));
        when(notificationTemplateRepository.findByTemplateCode("PAYMENT_SUCCESS-EMAIL")).thenReturn(Optional.of(template));
        when(notificationMessagesRepository.save(any(NotificationMessages.class))).thenAnswer(inv -> inv.getArgument(0));
        when(providerRegistry.getProvider(NotificationChannel.EMAIL)).thenReturn(Optional.of(mockEmailProvider));
        when(mockEmailProvider.send(any(NotificationMessages.class))).thenReturn(providerResult);

        NotificationResponse response = notificationService.sendNotification(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.getSubject()).isEqualTo("Payment Confirmed: ₹500.00");
        assertThat(response.getContent()).isEqualTo("Hello Bob, your payment of ₹500.00 is received.");

        verify(deliveryAttemptRepository, times(1)).save(any(DeliveryAttempt.class));
    }

    @Test
    @DisplayName("sendNotification: Should record FAILED status and increment retry count on provider failure")
    void sendNotification_ProviderFailure_RecordsFailedStatus() {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .idempotencyKey("idemp-provider-fail")
                .recipient("user@domain.com")
                .channel(NotificationChannel.EMAIL)
                .templateType(TemplateType.PAYMENT_FAILED)
                .customContent("Payment decline notice")
                .build();

        ProviderResult failResult = ProviderResult.failure("SMTP-Provider", "Connection timeout", 504, 3000);

        when(notificationMessagesRepository.findByIdempotencyKey("idemp-provider-fail")).thenReturn(Optional.empty());
        when(notificationMessagesRepository.save(any(NotificationMessages.class))).thenAnswer(inv -> inv.getArgument(0));
        when(providerRegistry.getProvider(NotificationChannel.EMAIL)).thenReturn(Optional.of(mockEmailProvider));
        when(mockEmailProvider.send(any(NotificationMessages.class))).thenReturn(failResult);

        NotificationResponse response = notificationService.sendNotification(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.getFailureReason()).isEqualTo("Connection timeout");
        assertThat(response.getRetryCount()).isEqualTo(1);

        verify(deliveryAttemptRepository, times(1)).save(any(DeliveryAttempt.class));
    }

    @Test
    @DisplayName("createTemplate: Should persist and return TemplateResponse")
    void createTemplate_Success() {
        CreateTemplateRequest request = CreateTemplateRequest.builder()
                .templateCode("OTP_ALERT-SMS")
                .templateType(TemplateType.OTP_ALERT)
                .channel(NotificationChannel.SMS)
                .bodyTemplate("Your PayFlow verification code is: {{otp}}")
                .active(true)
                .build();

        when(notificationTemplateRepository.save(any(NotificationTemplate.class)))
                .thenAnswer(inv -> {
                    NotificationTemplate t = inv.getArgument(0);
                    t.setId("tpl-doc-123");
                    return t;
                });

        TemplateResponse response = notificationService.createTemplate(request);

        assertThat(response).isNotNull();
        assertThat(response.getTemplateCode()).isEqualTo("OTP_ALERT-SMS");
        assertThat(response.getBodyTemplate()).contains("{{otp}}");
    }

    @Test
    @DisplayName("getNotificationByReference: Success")
    void getNotificationByReference_Success() {
        NotificationMessages msg = NotificationMessages.builder()
                .id("doc-1")
                .notificationReference("NOTIF-REF-001")
                .recipient("alice@payflow.com")
                .notificationChannel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .build();

        when(notificationMessagesRepository.findByNotificationReference("NOTIF-REF-001"))
                .thenReturn(Optional.of(msg));

        NotificationResponse response = notificationService.getNotificationByReference("NOTIF-REF-001");

        assertThat(response).isNotNull();
        assertThat(response.getNotificationReference()).isEqualTo("NOTIF-REF-001");
    }

    @Test
    @DisplayName("getNotificationByReference: Throws NotificationNotFoundException when not found")
    void getNotificationByReference_NotFound() {
        when(notificationMessagesRepository.findByNotificationReference("NOTIF-MISSING"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationByReference("NOTIF-MISSING"))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("updatePreference: Should update and persist merchant preferences")
    void updatePreference_Success() {
        UpdatePreferenceRequest request = UpdatePreferenceRequest.builder()
                .emailEnabled(true)
                .smsEnabled(true)
                .webhookEnabled(true)
                .webhookUrl("https://merchant.example.com/webhook")
                .webhookSecret("sec-wh-xyz")
                .build();

        when(notificationPreferenceRepository.findByMerchantId(101L)).thenReturn(Optional.empty());
        when(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PreferenceResponse response = notificationService.updatePreference(101L, request);

        assertThat(response).isNotNull();
        assertThat(response.isEmailEnabled()).isTrue();
        assertThat(response.isSmsEnabled()).isTrue();
        assertThat(response.isWebhookEnabled()).isTrue();
        assertThat(response.getWebhookUrl()).isEqualTo("https://merchant.example.com/webhook");
    }

    @Test
    @DisplayName("sendNotification: Should fallback render OTP content and subject when template is absent from Mongo")
    void sendNotification_OtpAlert_FallbackRendering_WhenTemplateAbsent() {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .idempotencyKey("idemp-otp-fallback-test")
                .recipient("testuser@payflow.com")
                .channel(NotificationChannel.EMAIL)
                .templateType(TemplateType.OTP_ALERT)
                .priority(NotificationPriority.HIGH)
                .payload(Map.of("otp", "789123", "purpose", "password reset"))
                .build();

        ProviderResult providerResult = ProviderResult.success("SMTP-Provider", "smtp-otp-999", 200, 50);

        when(notificationMessagesRepository.findByIdempotencyKey("idemp-otp-fallback-test")).thenReturn(Optional.empty());
        when(notificationTemplateRepository.findByTemplateCode("OTP_ALERT-EMAIL")).thenReturn(Optional.empty());
        when(notificationMessagesRepository.save(any(NotificationMessages.class))).thenAnswer(inv -> inv.getArgument(0));
        when(providerRegistry.getProvider(NotificationChannel.EMAIL)).thenReturn(Optional.of(mockEmailProvider));
        when(mockEmailProvider.send(any(NotificationMessages.class))).thenReturn(providerResult);

        NotificationResponse response = notificationService.sendNotification(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.getSubject()).contains("789123");
        assertThat(response.getContent()).contains("789123");
        assertThat(response.getContent()).contains("password reset");
    }

    @Test
    @DisplayName("sendNotification: Should fallback render Welcome Email content when template is absent")
    void sendNotification_WelcomeEmail_FallbackRendering_WhenTemplateAbsent() {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .idempotencyKey("idemp-welcome-fallback-test")
                .recipient("newuser@payflow.com")
                .channel(NotificationChannel.EMAIL)
                .templateType(TemplateType.WELCOME_EMAIL)
                .payload(Map.of("username", "bob_merchant"))
                .build();

        ProviderResult providerResult = ProviderResult.success("SMTP-Provider", "smtp-welcome-888", 200, 50);

        when(notificationMessagesRepository.findByIdempotencyKey("idemp-welcome-fallback-test")).thenReturn(Optional.empty());
        when(notificationTemplateRepository.findByTemplateCode("WELCOME_EMAIL-EMAIL")).thenReturn(Optional.empty());
        when(notificationMessagesRepository.save(any(NotificationMessages.class))).thenAnswer(inv -> inv.getArgument(0));
        when(providerRegistry.getProvider(NotificationChannel.EMAIL)).thenReturn(Optional.of(mockEmailProvider));
        when(mockEmailProvider.send(any(NotificationMessages.class))).thenReturn(providerResult);

        NotificationResponse response = notificationService.sendNotification(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.getSubject()).contains("bob_merchant");
        assertThat(response.getContent()).contains("bob_merchant");
    }
}
