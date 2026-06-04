package com.bitcoin.testnet.controller;

import com.bitcoin.testnet.dto.request.SendTransactionRequest;
import com.bitcoin.testnet.dto.response.TransactionResponse;
import com.bitcoin.testnet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** Envoie une transaction Bitcoin TestNet3 */
    @PostMapping("/send")
    public ResponseEntity<TransactionResponse> sendTransaction(
            @Valid @RequestBody SendTransactionRequest request) {
        log.info("Demande d'envoi: {} satoshis vers {}", request.getAmountSatoshis(), request.getToAddress());
        return ResponseEntity.ok(
                transactionService.sendTransaction(request.getToAddress(), request.getAmountSatoshis()));
    }

    /** Liste toutes les transactions persistées en MongoDB */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listTransactions() {
        return ResponseEntity.ok(transactionService.listTransactions());
    }

    /** Retourne une transaction par son hash */
    @GetMapping("/{txHash}")
    public ResponseEntity<TransactionResponse> getByHash(@PathVariable String txHash) {
        return transactionService.getByHash(txHash)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
