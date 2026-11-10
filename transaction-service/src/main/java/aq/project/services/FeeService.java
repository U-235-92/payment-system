package aq.project.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeService {

    @Value("${app.transaction.default_fee_percent}")
    private BigDecimal defaultFeePercent;

    public BigDecimal evaluateFeeForAmount(BigDecimal amount) {
        if(defaultFeePercent.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalStateException(
                    String.format("Illegal default fee percent value: [%s]", defaultFeePercent));

        return amount.multiply(defaultFeePercent);
    }
}
