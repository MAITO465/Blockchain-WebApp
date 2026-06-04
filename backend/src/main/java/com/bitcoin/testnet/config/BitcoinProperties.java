package com.bitcoin.testnet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de configuration pour le portefeuille Bitcoin.
 * Valeurs surchargées par variables d'environnement Docker.
 */
@Data
@ConfigurationProperties(prefix = "bitcoin.wallet")
public class BitcoinProperties {

    /** Répertoire où BitcoinJ stocke le fichier portefeuille (.wallet, .spvchain) */
    private String dir = "./wallet";

    /** Nom de base des fichiers portefeuille */
    private String name = "testnet-wallet";
}
