package com.bitcoin.testnet.kafka;

import com.bitcoin.testnet.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Producteur Kafka : publie les événements de transaction Bitcoin sur le topic configuré.
 * Utilise KafkaTemplate avec sérialisation JSON via JsonSerializer.
 */
@Component
@Slf4j
public class TransactionProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Value("${kafka.topics.bitcoin-transactions:bitcoin-transactions}")
    private String topic;

    public TransactionProducer(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publie un événement de transaction sur Kafka de façon asynchrone.
     * La clé du message est le txHash pour garantir l'ordre par transaction.
     */
    public void publishTransaction(TransactionEvent event) {
        log.info("Publication de l'événement Kafka - txHash: {}, topic: {}", event.getTxHash(), topic);

        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(topic, event.getTxHash(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Échec de la publication Kafka pour txHash: {} - Erreur: {}",
                        event.getTxHash(), ex.getMessage(), ex);
            } else {
                log.info("Événement Kafka publié avec succès - txHash: {}, partition: {}, offset: {}",
                        event.getTxHash(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
