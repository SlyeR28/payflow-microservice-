package com.payflow.notificationservice.service.impl;

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
import com.payflow.notificationservice.model.enums.NotificationStatus;
import com.payflow.notificationservice.provider.NotificationProvider;
import com.payflow.notificationservice.provider.ProviderRegistry;
import com.payflow.notificationservice.provider.ProviderResult;
import com.payflow.notificationservice.repository.DeliveryAttemptRepository;
import com.payflow.notificationservice.repository.NotificationMessagesRepository;
import com.payflow.notificationservice.repository.NotificationPreferenceRepository;
import com.payflow.notificationservice.repository.NotificationTemplateRepository;
import com.payflow.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl  implements NotificationService {

    private final NotificationMessagesRepository notificationMessagesRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final ProviderRegistry providerRegistry;
    private final TemplateEngineService templateEngineService;
    private final NotificationMapper notificationMapper;



    @Override
    public NotificationResponse sendNotification(SendNotificationRequest request) {
       // idempotency check
        Optional<NotificationMessages> existing = notificationMessagesRepository
                .findByIdempotencyKey(request.getIdempotencyKey());

        if (existing.isPresent()){
            log.info("Idempotent hit: notification with key [{}] already exists", request.getIdempotencyKey());
            return notificationMapper.toNotificationResponse(existing.get());
        }

        // 2 check Channel Preference for merchant
        if (request.getMerchantId() != null &&  !isChannelEnabled(request.getMerchantId(), request.getChannel())){
            log.warn("Channel [{}] is disabled for merchant [{}]", request.getChannel(), request.getMerchantId());
            throw new BusinessException("Notification Channel " +request.getChannel() + "is disabled for this merchant");
        }

        // resolve content (template or custom)
        String subject = request.getSubject();
        String content = request.getCustomContent();

        if (content == null || content.isBlank()) {
            String templateCode = request.getTemplateType().name() + "-" + request.getChannel().name();
            Optional<NotificationTemplate> byTemplateCode = notificationTemplateRepository.findByTemplateCode(templateCode);

            if (byTemplateCode.isPresent()) {
                NotificationTemplate tpl = byTemplateCode.get();
                subject = templateEngineService.render(tpl.getSubject(), request.getPayload());
                content = templateEngineService.render(tpl.getBodyTemplate(), request.getPayload());
            } else {
                content = "Notification Alert for " + request.getTemplateType();
            }

        }

            //Save Master record with Pending / processing status
            String reference = "NOTIF-" +   UUID.randomUUID()
                    .toString().
                    replace("-" , "").
                    substring(0 , 12).
                    toUpperCase();


            NotificationMessages message = NotificationMessages.builder()
                    .notificationReference(reference)
                    .idempotencyKey(request.getIdempotencyKey())
                    .merchantId(request.getMerchantId())
                    .recipient(request.getRecipient())
                    .notificationChannel(request.getChannel())
                    .templateType(request.getTemplateType())
                    .priority(request.getPriority())
                    .status(NotificationStatus.PROCESSING)
                    .subject(subject)
                    .content(content)
                    .payload(request.getPayload())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            notificationMessagesRepository.save(message);

            // 5 Lookup up Provider and dispatcher
            NotificationProvider provider = providerRegistry.getProvider(request.getChannel())
                    .orElseThrow(() -> new BusinessException("Provider not found for channel: " + request.getChannel()));

             ProviderResult result = provider.send(message);


        // 6. Record Delivery Attempt Audit Log
        DeliveryAttempt attempt = DeliveryAttempt.builder()
                .notificationId(message.getId())
                .notificationReference(message.getNotificationReference())
                .channel(request.getChannel())
                .providerName(result.getProviderName())
                .attemptNumber(1)
                .status(result.isSuccess() ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .responseStatusCode(result.getStatusCode())
                .responseBody(result.getResponseBody())
                .latencyMs(result.getLatencyMs())
                .createdAt(Instant.now())
                .build();


        deliveryAttemptRepository.save(attempt);

        // 7. Update Master Message Record
        if (result.isSuccess()) {
            message.setStatus(NotificationStatus.SENT);
            message.setSentAt(Instant.now());
            message.setProviderMessageId(result.getProviderMessageId());
        } else {
            message.setStatus(NotificationStatus.FAILED);
            message.setFailureReason(result.getResponseBody());
            message.setRetryCount(message.getRetryCount() + 1);
        }

        message.setUpdatedAt(Instant.now());

        message = notificationMessagesRepository.save(message);

        return notificationMapper.toNotificationResponse(message);

    }


    @Override
    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        NotificationTemplate template =
                NotificationTemplate.builder()
                        .templateCode(request.getTemplateCode())
                        .templateType(request.getTemplateType())
                        .channel(request.getChannel())
                        .subject(request.getSubject())
                        .bodyTemplate(request.getBodyTemplate())
                        .active(request.isActive())
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

        template = notificationTemplateRepository.save(template);
        return notificationMapper.toTemplateResponse( template);
    }

    @Override
    public NotificationResponse getNotificationByReference(String referenceId) {
       return notificationMapper.toNotificationResponse(notificationMessagesRepository.findByNotificationReference(referenceId).
                orElseThrow(() -> new NotificationNotFoundException(referenceId)));
    }

    @Override
    public PreferenceResponse getPreference(Long merchantId) {
     return notificationMapper.toPreferenceResponse(notificationPreferenceRepository.findByMerchantId(merchantId)
                .orElse(NotificationPreference.builder()
                        .merchantId(merchantId).build()));
    }


    @Override
    public PreferenceResponse updatePreference(Long merchantId, UpdatePreferenceRequest request) {
        NotificationPreference notificationPreference = notificationPreferenceRepository.findByMerchantId(merchantId)
                .orElse(NotificationPreference.builder()
                        .merchantId(merchantId).build());

        notificationPreference.setEmailEnabled(request.isEmailEnabled());
        notificationPreference.setSmsEnabled(request.isSmsEnabled());
        notificationPreference.setWhatsappEnabled(request.isWhatsappEnabled());
        notificationPreference.setWebhookEnabled(request.isWebhookEnabled());
        notificationPreference.setWebhookUrl(request.getWebhookUrl());
        notificationPreference.setWebhookSecret(request.getWebhookSecret());
        notificationPreference.setUpdatedAt(Instant.now());

        return notificationMapper.
                toPreferenceResponse(notificationPreferenceRepository.
                        save(notificationPreference));
    }

    private boolean isChannelEnabled(Long merchantId , NotificationChannel channel){
        return notificationPreferenceRepository.findByMerchantId(merchantId)
                .map(pref -> switch (channel) {
                    case EMAIL -> pref.isEmailEnabled();
                    case SMS -> pref.isSmsEnabled();
                    case WHATSAPP -> pref.isWhatsappEnabled();
                    case WEBHOOK -> pref.isWebhookEnabled();
                    case IN_APP -> true;
                }).orElse(true);
    }
}
