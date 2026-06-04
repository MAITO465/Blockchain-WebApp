package com.bitcoin.testnet.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration Kafka.
 * Crée automatiquement le topic bitcoin-transactions au démarrage via KafkaAdmin.
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.bitcoin-transactions:bitcoin-transactions}")
    private String transactionsTopic;

    /**
     * Crée le topic Kafka si il n'existe pas déjà.
     * partition=1, replication=1 convient pour un environnement local/Docker single-broker.
     */
    @Bean
    public NewTopic bitcoinTransactionsTopic() {
        return TopicBuilder.name(transactionsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
