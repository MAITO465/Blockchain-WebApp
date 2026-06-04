package com.bitcoin.testnet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private long satoshis;
    private String friendlyAmount;
    private String network;
    private boolean synced;
    private int bestChainHeight;
}
