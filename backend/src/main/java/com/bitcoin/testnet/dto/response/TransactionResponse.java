package com.bitcoin.testnet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String id;
    private String eventId;
    private String txHash;
    private String fromAddress;
    private String toAddress;
    private long amountSatoshis;
    private String status;
    private String network;
    private LocalDateTime createdAt;
}
