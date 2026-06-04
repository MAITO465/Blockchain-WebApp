package com.bitcoin.testnet.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Événement Kafka publié après chaque transaction Bitcoin diffusée avec succès.
 * Consommé par TransactionConsumer pour persister en MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    private String eventId;
    private String txHash;
    private String fromAddress;
    private String toAddress;
    private long amountSatoshis;
    private String status;
    private String network;
    private LocalDateTime timestamp;
}
