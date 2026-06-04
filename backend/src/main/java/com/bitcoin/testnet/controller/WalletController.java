package com.bitcoin.testnet.controller;

import com.bitcoin.testnet.dto.response.AddressBalanceResponse;
import com.bitcoin.testnet.dto.response.AddressResponse;
import com.bitcoin.testnet.dto.response.BalanceResponse;
import com.bitcoin.testnet.service.BlockExplorerService;
import com.bitcoin.testnet.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@Slf4j
public class WalletController {

    private final WalletService walletService;
    private final BlockExplorerService blockExplorerService;

    public WalletController(WalletService walletService, BlockExplorerService blockExplorerService) {
        this.walletService = walletService;
        this.blockExplorerService = blockExplorerService;
    }

    /** Génère une nouvelle adresse de réception TestNet3 */
    @PostMapping("/address")
    public ResponseEntity<AddressResponse> generateAddress() {
        return ResponseEntity.ok(walletService.generateAddress());
    }

    /** Retourne le solde total du portefeuille */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {
        return ResponseEntity.ok(walletService.getBalance());
    }

    /** Liste toutes les adresses générées et persistées */
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> listAddresses() {
        return ResponseEntity.ok(walletService.listAddresses());
    }

    /** Retourne le solde d'une adresse spécifique via Blockstream */
    @GetMapping("/address/{address}/balance")
    public ResponseEntity<AddressBalanceResponse> getAddressBalance(@PathVariable String address) {
        return ResponseEntity.ok(blockExplorerService.getAddressBalance(address));
    }
}
