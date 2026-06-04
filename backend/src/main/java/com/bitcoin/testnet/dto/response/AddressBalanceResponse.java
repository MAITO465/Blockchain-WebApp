package com.bitcoin.testnet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBalanceResponse {
    private String address;
    private long confirmedSatoshis;
    private long unconfirmedSatoshis;
    private long totalSatoshis;
    private String friendlyTotal;
}
