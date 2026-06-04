package com.bitcoin.testnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyMessageRequest {

    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    @NotBlank(message = "Le message est obligatoire")
    private String message;

    @NotBlank(message = "La signature est obligatoire")
    private String signature;
}
