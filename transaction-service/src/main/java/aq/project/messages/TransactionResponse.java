package aq.project.messages;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode
public class TransactionResponse {

    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String transactionId;

    @NotNull
    private OperationType operationType;

    @NotNull
    private TransactionStatus transactionStatus;

    @NotNull
    private Long timestamp;

    private Map<String, Object> properties = new HashMap<>();
}
