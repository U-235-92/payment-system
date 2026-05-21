package aq.project.controllers;

import aq.project.dto.TransactionStatus;
import aq.project.exceptions.OutboxEventException;
import aq.project.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    @GetMapping("/status/{transactionId}")
    public TransactionStatus getTransactionStatus(@PathVariable String transactionId) throws OutboxEventException {
        return transactionService.getTransactionStatus(transactionId);
    }
}
