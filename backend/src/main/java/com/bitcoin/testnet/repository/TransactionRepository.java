package com.bitcoin.testnet.repository;

import com.bitcoin.testnet.document.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<TransactionDocument, String> {
    Optional<TransactionDocument> findByTxHash(String txHash);
    boolean existsByTxHash(String txHash);
    java.util.List<TransactionDocument> findByUserId(String userId);
}
