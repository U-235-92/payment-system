package aq.project.services;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.dto.Status;
import aq.project.entities.Payment;
import aq.project.fake_payment_provider_service.PayoutApiClient;
import aq.project.mappers.PaymentMapper;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final PayoutApiClient payoutClient;

    private final PaymentRepository paymentRepository;

    private final AuthorizationService authorizationService;

    private final TraceContext traceContext;

    @Transactional
    public void handleProcessingPayoutPayment(
            Payment payment
    ) {
        Long payoutId = payment.getExternalId();

        String authorization = authorizationService.getAuthorization();
        String traceId = traceContext.getTraceId() == null ? TraceFactory.genTraceId() : traceContext.getTraceId();

        log.debug(">>>>> Attempt to send get payout info request to [fake-payment-provider-service]");

        ResponseEntity<PayoutInfoResponseDto> responseEntity = payoutClient
                .getPayoutInfo(payoutId, authorization, traceId);

        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        log.debug(">>>>> Received response on get payout info request from [fake-payment-provider-service] " +
                "with status code: {}", httpStatusCode);

        if(httpStatusCode.is2xxSuccessful()) {
            PayoutInfoResponseDto payoutInfoResponseDto = responseEntity.getBody();

            log.debug(">>>>> Received response on get payout info request from [fake-payment-provider-service] " +
                    "with body: {}", payoutInfoResponseDto);

            if(payoutInfoResponseDto != null) {
                Status payoutStatus = payoutInfoResponseDto.getStatus();

                switch (payoutStatus) {
                    case SUCCESS -> {
//                        The final process of payment by sending result of operation into individuals-api-service
//                        See PaymentScheduler.scheduleCompletePayment(..) for details
                        payment.complete();

                        paymentRepository.save(payment);
                    }

                    case FAIL -> {
//                        The final process of payment by sending result of operation into individuals-api-service
//                        See PaymentScheduler.scheduleFailPayment(..) for details
                        payment.fail();

                        paymentRepository.save(payment);
                    }
                }
            } else {
                log.error("[{}]: Received response has no body", traceId);
            }
        }
    }

    @Transactional
    public void handlePendingPayoutPayment(
            Payment payment
    ) {
        String traceId = TraceFactory.genTraceId();
        String authorization = authorizationService.getAuthorization();
        String merchantId = authorizationService.getMerchantId();

        CreatePayoutRequestDto createPayoutRequestDto = paymentMapper
                .toCreatePayoutRequestDto(payment);

        createPayoutRequestDto.setMerchantId(merchantId);

        log.debug(">>>>> Attempt to send [payout request] into [fake-payment-provider-service]");

        ResponseEntity<PayoutInfoResponseDto> responseEntity = payoutClient
                .createPayout(authorization, createPayoutRequestDto, traceId);

        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        log.debug(">>>>> Received response on [payout request] from [fake-payment-provider-service] " +
                "with status code: {}", httpStatusCode);

        if(httpStatusCode.is2xxSuccessful()) {
//            The final process of payment to success/fail state will continue on PaymentStatusKafkaConsumer side
//            See PaymentStatusKafkaConsumer.onPaymentStatusEvent(..) for details
            PayoutInfoResponseDto payoutInfoResponseDto = responseEntity.getBody();

            log.debug(">>>>> Received response on [payout request] from [fake-payment-provider-service] " +
                    "with body: {}", payoutInfoResponseDto);

            if(payoutInfoResponseDto != null) {
                Long payoutId = payoutInfoResponseDto.getId();

                payment.setExternalId(payoutId);
                payment.processing();

                paymentRepository.save(payment);
            } else {
                throw new IllegalStateException("Received response has no body");
            }
        }
    }
}