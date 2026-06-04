package com.bitcoin.testnet.kafka;

import com.bitcoin.testnet.document.TransactionDocument;
import com.bitcoin.testnet.event.TransactionEvent;
import com.bitcoin.testnet.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consommateur Kafka : écoute le topic bitcoin-transactions.
 * Persiste chaque événement reçu dans MongoDB.
 * Vérifie l'idempotence par txHash pour éviter les doublons.
 */
@Component
@Slf4j
public class TransactionConsumer {

    private final TransactionRepository transactionRepository;

    public TransactionConsumer(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Réception d'un événement de transaction Bitcoin depuis Kafka.
     * group-id défini dans application.yml : bitcoin-consumer-group
     */
    @KafkaListener(
            topics = "${kafka.topics.bitcoin-transactions:bitcoin-transactions}",
            groupId = "${spring.kafka.consumer.group-id:bitcoin-consumer-group}"
    )
    public void consume(TransactionEvent event) {

        log.info("Événement Kafka reçu - txHash: {}", event.getTxHash());

        // Vérification d'idempotence : éviter la double persisistance du même txHash
        if (transactionRepository.existsByTxHash(event.getTxHash())) {
            log.warn("Transaction déjà persistée, ignorée - txHash: {}", event.getTxHash());
            return;
        }

        TransactionDocument doc = TransactionDocument.builder()
                .eventId(event.getEventId())
                .txHash(event.getTxHash())
                .fromAddress(event.getFromAddress())
                .toAddress(event.getToAddress())
                .amountSatoshis(event.getAmountSatoshis())
                .status(event.getStatus())
                .network(event.getNetwork())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            transactionRepository.save(doc);
            log.info("Transaction persistée en MongoDB - txHash: {}", event.getTxHash());
        } catch (Exception e) {
            log.error("Erreur lors de la persistance MongoDB - txHash: {} - Erreur: {}",
                    event.getTxHash(), e.getMessage(), e);
            throw e;
        }
    }
}
