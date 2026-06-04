package com.bitcoin.testnet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendTransactionRequest {

    @NotBlank(message = "L'adresse de destination est obligatoire")
    private String toAddress;

    @Min(value = 1, message = "Le montant doit être supérieur à 0 satoshi")
    private long amountSatoshis;
}
