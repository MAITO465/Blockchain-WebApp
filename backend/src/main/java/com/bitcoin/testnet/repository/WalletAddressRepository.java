package com.bitcoin.testnet.repository;

import com.bitcoin.testnet.document.WalletAddress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletAddressRepository extends MongoRepository<WalletAddress, String> {
    Optional<WalletAddress> findByAddress(String address);
    boolean existsByAddress(String address);
    java.util.List<WalletAddress> findByUserId(String userId);
    Optional<WalletAddress> findByUserIdAndAddress(String userId, String address);
}
