package com.bitcoin.testnet.service;

import com.bitcoin.testnet.dto.response.AddressBalanceResponse;
import com.bitcoin.testnet.exception.BitcoinException;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class BlockExplorerService {

    private final RestTemplate restTemplate;

    public BlockExplorerService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Utilise l'API publique de Blockstream pour obtenir le solde d'une adresse TestNet3.
     */
    public AddressBalanceResponse getAddressBalance(String address) {
        String url = "https://blockstream.info/testnet/api/address/" + address;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("chain_stats") || !body.containsKey("mempool_stats")) {
                throw new BitcoinException("Réponse invalide de l'API Blockstream");
            }

            Map<String, Object> chainStats = (Map<String, Object>) body.get("chain_stats");
            Map<String, Object> mempoolStats = (Map<String, Object>) body.get("mempool_stats");

            long fundedTxoSum = getLong(chainStats, "funded_txo_sum");
            long spentTxoSum = getLong(chainStats, "spent_txo_sum");
            long confirmedSatoshis = fundedTxoSum - spentTxoSum;

            long mempoolFunded = getLong(mempoolStats, "funded_txo_sum");
            long mempoolSpent = getLong(mempoolStats, "spent_txo_sum");
            long unconfirmedSatoshis = mempoolFunded - mempoolSpent;

            long totalSatoshis = confirmedSatoshis + unconfirmedSatoshis;

            return AddressBalanceResponse.builder()
                    .address(address)
                    .confirmedSatoshis(confirmedSatoshis)
                    .unconfirmedSatoshis(unconfirmedSatoshis)
                    .totalSatoshis(totalSatoshis)
                    .friendlyTotal(Coin.valueOf(totalSatoshis).toFriendlyString())
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la balance pour l'adresse {}: {}", address, e.getMessage());
            throw new BitcoinException("Impossible de récupérer la balance pour l'adresse: " + address);
        }
    }

    private long getLong(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return 0;
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return 0;
    }
}
