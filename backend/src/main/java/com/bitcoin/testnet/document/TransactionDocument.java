package com.bitcoin.testnet.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class TransactionDocument {

    @Id
    private String id;

    private String eventId;

    @Indexed(unique = true)
    private String txHash;

    private String fromAddress;
    private String toAddress;
    private long amountSatoshis;
    private String status;
    private String network;

    @CreatedDate
    private LocalDateTime createdAt;
}
