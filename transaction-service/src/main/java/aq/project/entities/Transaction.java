package aq.project.entities;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @NotBlank
    @Column(name = "id", nullable = false)
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @NotNull
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @PositiveOrZero
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "processed", nullable = false)
    private boolean isProcessed;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "metadata_id", nullable = false)
    private TransactionMetadata metadata;

    @Column(name = "property_value")
    @MapKeyColumn(name = "property_key")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "transactions_properties")
    private Map<String, String> properties = new HashMap<>();
}
