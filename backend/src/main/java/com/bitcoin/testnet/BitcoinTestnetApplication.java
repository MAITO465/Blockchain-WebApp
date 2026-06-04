package com.bitcoin.testnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@ConfigurationPropertiesScan
public class BitcoinTestnetApplication {

    public static void main(String[] args) {
        SpringApplication.run(BitcoinTestnetApplication.class, args);
    }
}
