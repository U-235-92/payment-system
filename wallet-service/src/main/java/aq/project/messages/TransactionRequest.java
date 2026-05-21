package aq.project.messages;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.UnknownMessagePropertyException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
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
    private Long timestamp;

    private final Map<String, Object> properties = new HashMap<>();;;

    public TransactionRequest(String transactionId, OperationType operationType, BigDecimal amount, String currency, TransactionStatus transactionStatus, Long timestamp) {
        super();
        this.transactionId = transactionId;
        this.operationType = operationType;
        this.amount = amount;
        this.currency = currency;
        this.transactionStatus = transactionStatus;
        this.timestamp = timestamp;
    }

    public <T> T getProperty(String key, Class<T> type) {
        if(isPropertyNull(key))
            throw new UnknownMessagePropertyException("Unknown message property: " + key);
        if(!type.isInstance(properties.get(key)))
            throw new ClassCastException(String.format("Message property type [%s] is not of type [%s]", properties.get(key).getClass().getName(), type.getName()));
        return (T) properties.get(key);
    }

    public void putProperty(String key, Object value) {
        if(value != null && key != null)
            properties.put(key, value);
    }

    public boolean isPropertyNull(String key) {
        return properties.get(key) == null;
    }
}
