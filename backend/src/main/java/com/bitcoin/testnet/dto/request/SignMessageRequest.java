package com.bitcoin.testnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignMessageRequest {

    @NotBlank(message = "L'adresse du portefeuille est obligatoire")
    private String address;

    @NotBlank(message = "Le message à signer est obligatoire")
    private String message;
}
