package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.TransactionStatus;
import aq.project.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    public ResponseEntity<TransactionStatus> getTransactionStatus(String id) {
        return ResponseEntity.ok(transactionService.getTransactionStatus(id));
    }
}
