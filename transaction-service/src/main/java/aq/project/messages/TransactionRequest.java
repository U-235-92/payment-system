package aq.project.messages;

import aq.project.exceptions.UnknownMessagePropertyException;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@NoArgsConstructor
public class TransactionRequest {

    @NotBlank
    @Getter @Setter
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String transactionId;

    @NotNull
    @Getter @Setter
    private OperationType operationType;

    @NotNull
    @Getter @Setter
    private BigDecimal amount;

    @NotBlank
    @Getter @Setter
    @Pattern(regexp = "[A-Z]{3}")
    private String currency;

    @NotNull
    @Getter @Setter
    private TransactionStatus transactionStatus;

    @NotNull
    @Getter @Setter
    @PositiveOrZero
    private Long timestamp;

    private final Map<String, String> properties = new HashMap<>();;;

    public TransactionRequest(String transactionId, OperationType operationType, BigDecimal amount, String currency, TransactionStatus transactionStatus, Long timestamp) {
        super();
        this.transactionId = transactionId;
        this.operationType = operationType;
        this.amount = amount;
        this.currency = currency;
        this.transactionStatus = transactionStatus;
        this.timestamp = timestamp;
    }

    public String getProperty(String key) {
        if(isPropertyNull(key))
            throw new UnknownMessagePropertyException("Unknown message property: " + key);
        return properties.get(key);
    }

    public boolean isPropertyNull(String key) {
        return properties.get(key) == null;
    }

    public void putProperty(String key, String value) {
        if(value != null && key != null)
            properties.put(key, value);
    }

    public void copyProperties(Map<String, String> properties) {
        properties.forEach(this::putProperty);
    }
}
