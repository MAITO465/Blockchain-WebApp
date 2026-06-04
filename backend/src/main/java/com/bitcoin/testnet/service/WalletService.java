package com.bitcoin.testnet.service;

import com.bitcoin.testnet.document.WalletAddress;
import com.bitcoin.testnet.dto.response.AddressResponse;
import com.bitcoin.testnet.dto.response.BalanceResponse;
import com.bitcoin.testnet.repository.WalletAddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WalletService {

    private final BitcoinService bitcoinService;
    private final WalletAddressRepository walletAddressRepository;

    public WalletService(BitcoinService bitcoinService,
                         WalletAddressRepository walletAddressRepository) {
        this.bitcoinService = bitcoinService;
        this.walletAddressRepository = walletAddressRepository;
    }

    /**
     * Génère une nouvelle adresse de réception, la persiste en MongoDB et la retourne.
     */
    public AddressResponse generateAddress() {
        Address address = bitcoinService.generateFreshAddress();
        String addressStr = address.toString();

        // Persistance en MongoDB (si pas déjà enregistrée)
        WalletAddress walletAddress;
        if (walletAddressRepository.existsByAddress(addressStr)) {
            walletAddress = walletAddressRepository.findByAddress(addressStr).orElseThrow();
            log.debug("Adresse déjà en base: {}", addressStr);
        } else {
            walletAddress = WalletAddress.builder()
                    .address(addressStr)
                    .network("TESTNET3")
                    .createdAt(LocalDateTime.now())
                    .build();
            walletAddress = walletAddressRepository.save(walletAddress);
            log.info("Nouvelle adresse persistée en MongoDB: {}", addressStr);
        }

        return toResponse(walletAddress);
    }

    /**
     * Retourne le solde total du portefeuille.
     *
     * Note sur la limitation SPV :
     * En mode SPV, le solde correspond au total du portefeuille local.
     * Il n'est pas possible d'obtenir le solde d'une adresse individuelle
     * de façon fiable sans interroger un explorateur de blocs externe.
     * Le solde peut ne pas être exact si la synchronisation est incomplète.
     */
    public BalanceResponse getBalance() {
        Coin balance = bitcoinService.getBalance();
        return BalanceResponse.builder()
                .satoshis(balance.getValue())
                .friendlyAmount(balance.toFriendlyString())
                .network("TESTNET3")
                .synced(bitcoinService.isSynced())
                .bestChainHeight(bitcoinService.getBestChainHeight())
                .build();
    }

    /**
     * Liste toutes les adresses générées et persistées dans MongoDB.
     */
    public List<AddressResponse> listAddresses() {
        return walletAddressRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AddressResponse toResponse(WalletAddress wa) {
        return AddressResponse.builder()
                .id(wa.getId())
                .address(wa.getAddress())
                .network(wa.getNetwork())
                .createdAt(wa.getCreatedAt())
                .build();
    }
}
