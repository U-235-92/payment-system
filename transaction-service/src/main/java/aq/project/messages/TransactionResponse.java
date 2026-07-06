package aq.project.messages;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
public class TransactionResponse {

    @NotBlank
    @Getter @Setter
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String transactionId;

    @Getter
    @NotNull
    private OperationType operationType;

    @NotNull
    @Getter @Setter
    private TransactionStatus transactionStatus;

    @NotNull
    @Getter @Setter
    private Long timestamp;

    private Map<String, Object> properties = new HashMap<>();;;

    public TransactionResponse(String transactionId, OperationType operationType, TransactionStatus transactionStatus) {
        super();
        this.transactionId = transactionId;
        this.operationType = operationType;
        this.transactionStatus = transactionStatus;
    }
}
