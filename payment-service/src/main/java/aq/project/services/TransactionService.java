package aq.project.services;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.Status;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.entities.Payment;
import aq.project.fake_payment_provider_service.TransactionApiClient;
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
public class TransactionService {

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final TransactionApiClient transactionClient;

    private final PaymentRepository paymentRepository;

    private final AuthorizationService authorizationService;

    private final TraceContext traceContext;

    @Transactional
    public void handleProcessingTransactionPayment(
            Payment payment
    ) {
        Long payoutId = payment.getExternalId();

        String authorization = authorizationService.getAuthorization();
        String traceId = traceContext.getTraceId() == null ? TraceFactory.genTraceId() : traceContext.getTraceId();

        log.debug(">>>>> Attempt to send get transaction info request to [fake-payment-provider-service]");

        ResponseEntity<TransactionInfoResponseDto> responseEntity = transactionClient
                .getTransactionInfo(payoutId, authorization, traceId);

        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        log.debug(">>>>> Received response on get transaction info request from [fake-payment-provider-service] " +
                "with status code: {}", httpStatusCode);

        if(httpStatusCode.is2xxSuccessful()) {
            TransactionInfoResponseDto transactionInfoResponseDto = responseEntity.getBody();

            log.debug(">>>>> Received response on get transaction info request from [fake-payment-provider-service] " +
                    "with body: {}", transactionInfoResponseDto);

            if(transactionInfoResponseDto != null) {
                Status payoutStatus = transactionInfoResponseDto.getStatus();

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
    public void handlePendingTransactionPayment(
            Payment payment
    ) {
        String traceId = TraceFactory.genTraceId();
        String authorization = authorizationService.getAuthorization();
        String merchantId = authorizationService.getMerchantId();

        CreateTransactionRequestDto createTransactionRequestDto = paymentMapper
                .toCreateTransactionRequestDto(payment);

        createTransactionRequestDto.setMerchantId(merchantId);

        log.debug(">>>>> Attempt to send [transaction request] into [fake-payment-provider-service]");

        ResponseEntity<TransactionInfoResponseDto> responseEntity = transactionClient
                .createTransaction(authorization, createTransactionRequestDto, traceId);

        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        log.debug(">>>>> Received response on [transaction request] from [fake-payment-provider-service] " +
                "with status code: {}", httpStatusCode);

        if(httpStatusCode.is2xxSuccessful()) {
//            The final process of payment to success/fail state will continue on PaymentStatusKafkaConsumer side
//            See PaymentStatusKafkaConsumer.onPaymentStatusEvent(..) for details
            TransactionInfoResponseDto transactionInfoResponseDto = responseEntity.getBody();

            log.debug(">>>>> Received response on [transaction request] from [fake-payment-provider-service] " +
                    "with body: {}", transactionInfoResponseDto);

            if(transactionInfoResponseDto != null) {
                Long transactionId = transactionInfoResponseDto.getId();

                payment.setExternalId(transactionId);
                payment.processing();

                paymentRepository.save(payment);
            } else {
                throw new IllegalStateException("Received response has no body");
            }
        }
    }
}