package com.bitcoin.testnet.service;

import com.bitcoin.testnet.document.TransactionDocument;
import com.bitcoin.testnet.dto.response.TransactionResponse;
import com.bitcoin.testnet.event.TransactionEvent;
import com.bitcoin.testnet.kafka.TransactionProducer;
import com.bitcoin.testnet.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import com.bitcoin.testnet.document.User;
import com.bitcoin.testnet.document.WalletAddress;
import com.bitcoin.testnet.repository.WalletAddressRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    private final BitcoinService bitcoinService;
    private final TransactionProducer producer;
    private final TransactionRepository transactionRepository;
    private final WalletAddressRepository walletAddressRepository;

    public TransactionService(BitcoinService bitcoinService,
                               TransactionProducer producer,
                               TransactionRepository transactionRepository,
                               WalletAddressRepository walletAddressRepository) {
        this.bitcoinService = bitcoinService;
        this.producer = producer;
        this.transactionRepository = transactionRepository;
        this.walletAddressRepository = walletAddressRepository;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Envoie une transaction Bitcoin et publie l'événement sur Kafka.
     * La persistance MongoDB est effectuée par le consumer Kafka.
     */
    public TransactionResponse sendTransaction(String toAddress, long satoshis) {
        User user = getCurrentUser();
        List<String> userAddresses = walletAddressRepository.findByUserId(user.getId())
                .stream()
                .map(WalletAddress::getAddress)
                .collect(Collectors.toList());

        if (userAddresses.isEmpty()) {
            throw new RuntimeException("Vous n'avez généré aucune adresse.");
        }

        // 1. Récupérer l'adresse source (à titre indicatif, on prend la première générée par l'utilisateur)
        String fromAddress = userAddresses.get(0);

        // 2. Diffuser la transaction sur TestNet3 via BitcoinJ en limitant les UTXOs
        String txHash = bitcoinService.sendTransaction(toAddress, satoshis, userAddresses);

        // 3. Créer et publier l'événement Kafka
        TransactionEvent event = TransactionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .txHash(txHash)
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .amountSatoshis(satoshis)
                .status("BROADCAST")
                .network("TESTNET3")
                .timestamp(LocalDateTime.now())
                .build();

        producer.publishTransaction(event);

        log.info("Transaction envoyée et événement Kafka publié - txHash: {}", txHash);

        // Persistance locale immédiate pour garantir que le userId est bien associé,
        // (Le consumer Kafka pourrait aussi l'associer s'il avait le userId, mais le consumer est asynchrone).
        TransactionDocument doc = TransactionDocument.builder()
                .eventId(event.getEventId())
                .txHash(txHash)
                .userId(user.getId())
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .amountSatoshis(satoshis)
                .status("BROADCAST")
                .network("TESTNET3")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(doc);

        return TransactionResponse.builder()
                .eventId(event.getEventId())
                .txHash(txHash)
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .amountSatoshis(satoshis)
                .status("BROADCAST")
                .network("TESTNET3")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Liste toutes les transactions persistées en MongoDB pour l'utilisateur actuel.
     */
    public List<TransactionResponse> listTransactions() {
        User user = getCurrentUser();
        return transactionRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retourne une transaction par son hash (si persistée).
     */
    public Optional<TransactionResponse> getByHash(String txHash) {
        return transactionRepository.findByTxHash(txHash)
                .map(this::toResponse);
    }

    private TransactionResponse toResponse(TransactionDocument doc) {
        return TransactionResponse.builder()
                .id(doc.getId())
                .eventId(doc.getEventId())
                .txHash(doc.getTxHash())
                .fromAddress(doc.getFromAddress())
                .toAddress(doc.getToAddress())
                .amountSatoshis(doc.getAmountSatoshis())
                .status(doc.getStatus())
                .network(doc.getNetwork())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
