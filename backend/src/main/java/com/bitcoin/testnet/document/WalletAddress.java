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
@Document(collection = "wallet_addresses")
public class WalletAddress {

    @Id
    private String id;

    @Indexed(unique = true)
    private String address;

    private String network;

    @CreatedDate
    private LocalDateTime createdAt;
}
