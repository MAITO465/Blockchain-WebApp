package com.bitcoin.testnet.service;

import com.bitcoin.testnet.document.TransactionDocument;
import com.bitcoin.testnet.dto.response.TransactionResponse;
import com.bitcoin.testnet.event.TransactionEvent;
import com.bitcoin.testnet.kafka.TransactionProducer;
import com.bitcoin.testnet.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
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

    public TransactionService(BitcoinService bitcoinService,
                               TransactionProducer producer,
                               TransactionRepository transactionRepository) {
        this.bitcoinService = bitcoinService;
        this.producer = producer;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Envoie une transaction Bitcoin et publie l'événement sur Kafka.
     * La persistance MongoDB est effectuée par le consumer Kafka.
     */
    public TransactionResponse sendTransaction(String toAddress, long satoshis) {
        // 1. Récupérer l'adresse source (à titre indicatif)
        String fromAddress = bitcoinService.getCurrentReceiveAddress();

        // 2. Diffuser la transaction sur TestNet3 via BitcoinJ
        String txHash = bitcoinService.sendTransaction(toAddress, satoshis);

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
     * Liste toutes les transactions persistées en MongoDB.
     */
    public List<TransactionResponse> listTransactions() {
        return transactionRepository.findAll()
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
