package aq.project.entities.payment_provider_service;

import aq.project.dto.Operation;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@Table(name = "payment_provider_service_transaction_requests")
public class PaymentProviderServiceTransactionRequest {

    @Id
    @NotNull
    @Column(name = "transaction_id")
    private UUID transactionId;

    @NotBlank
    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private Operation operation;

    @NotNull
    @Positive
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotBlank
    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @NotBlank
    @Column(name = "notification_url", nullable = false)
    private String notificationUrl;

    @Nullable
    @Column(name = "description")
    private String description;

    @JoinColumn(name = "metadata_id")
    @OneToOne(cascade = CascadeType.ALL)
    private PaymentProviderServiceTransactionRequestMetadata transactionRequestMetadata;
}
