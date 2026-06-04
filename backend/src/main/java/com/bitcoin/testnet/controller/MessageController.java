package com.bitcoin.testnet.controller;

import com.bitcoin.testnet.dto.request.SignMessageRequest;
import com.bitcoin.testnet.dto.request.VerifyMessageRequest;
import com.bitcoin.testnet.dto.response.SignatureResponse;
import com.bitcoin.testnet.dto.response.VerifyResponse;
import com.bitcoin.testnet.service.BitcoinService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@Slf4j
public class MessageController {

    private final BitcoinService bitcoinService;

    public MessageController(BitcoinService bitcoinService) {
        this.bitcoinService = bitcoinService;
    }

    /** Signe un message avec la clé privée associée à une adresse du portefeuille */
    @PostMapping("/sign")
    public ResponseEntity<SignatureResponse> signMessage(
            @Valid @RequestBody SignMessageRequest request) {
        log.info("Signature de message pour l'adresse: {}", request.getAddress());
        String signature = bitcoinService.signMessage(request.getAddress(), request.getMessage());
        return ResponseEntity.ok(SignatureResponse.builder()
                .address(request.getAddress())
                .message(request.getMessage())
                .signature(signature)
                .build());
    }

    /** Vérifie une signature de message Bitcoin */
    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verifyMessage(
            @Valid @RequestBody VerifyMessageRequest request) {
        log.info("Vérification de signature pour l'adresse: {}", request.getAddress());
        boolean valid = bitcoinService.verifyMessage(
                request.getAddress(), request.getMessage(), request.getSignature());
        return ResponseEntity.ok(VerifyResponse.builder()
                .valid(valid)
                .address(request.getAddress())
                .message(valid ? "Signature valide" : "Signature invalide")
                .build());
    }
}
