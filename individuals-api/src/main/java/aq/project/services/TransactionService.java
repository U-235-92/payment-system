package aq.project.services;

import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import aq.project.proxies.TransactionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionClient transactionClient;

    public Mono<TransactionStatus> getTransactionStatus(String transactionId) {
        return transactionClient.getTransactionStatus(transactionId);
    }

    public Mono<String> doTransaction(TransactionRequestDTO dto) {
        return transactionClient.doTransaction(dto);
    }
}
