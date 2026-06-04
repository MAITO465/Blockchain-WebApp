# Client Web Blockchain Bitcoin TestNet

A full-stack web application to interact with **Bitcoin TestNet3** through a browser.
Built with Java 17 + Spring Boot 3, Angular 17, Apache Kafka, MongoDB, and BitcoinJ 0.16.x.
Fully dockerized — runs with a single command.

---

## What it does

| Feature | Description |
|---|---|
| HD Wallet | Creates/loads a BitcoinJ SPV wallet on TestNet3 |
| Generate Addresses | Fresh P2PKH receiving addresses (BIP44 HD derivation) |
| Wallet Balance | Total balance with sync status |
| Send Transactions | Broadcast transactions to TestNet3 network |
| Kafka Events | Every broadcast publishes a Kafka event |
| MongoDB Persistence | Kafka consumer persists transactions and addresses |
| Sign Message | Sign a message with a wallet key (Bitcoin standard format) |
| Verify Message | Verify a Bitcoin message signature |
| Angular Frontend | Clean SPA served via Nginx |

---

## Architecture

```
Browser
  └─► Nginx (port 4200)
        ├─ Serves Angular SPA (static files)
        └─ /api/* → Proxy → Spring Boot Backend (port 8080)
                              ├─ BitcoinJ (SPV, TestNet3 P2P network)
                              ├─ Kafka Producer → bitcoin-transactions topic
                              ├─ Kafka Consumer → MongoDB
                              └─ MongoDB (wallet addresses + transactions)
```

### Containers

| Container | Image | Role | Port |
|---|---|---|---|
| `bitcoin-frontend` | Custom (Nginx + Angular) | Serves UI, proxies `/api` | 4200 |
| `bitcoin-backend` | Custom (Java 17 + Spring Boot) | REST API, BitcoinJ, Kafka | 8080 |
| `bitcoin-mongodb` | mongo:7.0 | Persists addresses and transactions | 27017 |
| `bitcoin-kafka` | confluentinc/cp-kafka:7.5.0 | Message broker | 9092 (ext), 29092 (int) |
| `bitcoin-zookeeper` | confluentinc/cp-zookeeper:7.5.0 | Kafka coordination | 2181 |

---

## Docker Networking

All containers share the `bitcoin-network` bridge network.

- **Backend** connects to MongoDB using hostname `mongodb` (Docker DNS).
- **Backend** connects to Kafka using `kafka:29092` (internal PLAINTEXT listener).
- **Frontend/Nginx** proxies `/api` to `http://backend:8080` (Docker DNS).

### Kafka Listeners

| Listener | Address | Used by |
|---|---|---|
| `INTERNAL` | `kafka:29092` | Spring Boot backend (inside Docker) |
| `EXTERNAL` | `localhost:9092` | Host machine debugging tools |

---

## Docker Volumes

| Volume | Container path | Purpose |
|---|---|---|
| `mongodb-data` | `/data/db` | MongoDB data persists between restarts |
| `bitcoin-wallet` | `/app/wallet` | BitcoinJ wallet files (`.wallet`, `.spvchain`) |

Wallet files persist so you don't lose your wallet between container restarts.

---

## How to Run

### Prerequisites

- Docker Engine 24+
- Docker Compose v2+
- Internet access (BitcoinJ connects to TestNet3 peers)

### Start everything

```bash
cd client-web-blockchain-bitcoin-testnet
docker compose up --build
```

First build downloads Maven/npm dependencies — takes ~5 minutes.
Subsequent builds use Docker cache and are much faster.

### Wait for services

Watch the logs. When you see:

```
bitcoin-backend  | BitcoinJ WalletAppKit démarré avec succès sur TestNet3
```

The backend is ready. BitcoinJ will continue syncing block headers in the background.

### Access the application

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080/api |
| Health check | http://localhost:8080/actuator/health |
| MongoDB | localhost:27017 |
| Kafka (external) | localhost:9092 |
| Zookeeper | localhost:2181 |

---

## How to Stop

```bash
docker compose down
```

Data volumes are preserved.

## Delete volumes (reset all data)

```bash
docker compose down -v
```

**Warning:** This deletes the Bitcoin wallet files. You will lose any TestNet funds.

---

## Step-by-Step Usage

### 1. Generate a receiving address

Go to **Addresses** → click **Generate Address**.
An address like `mXXXXXXX...` (legacy TestNet) is created and saved to MongoDB.

### 2. Get TestNet coins from a faucet

Copy the generated address and paste it at one of these faucets:
- https://coinfaucet.eu/en/btc-testnet
- https://testnet-faucet.com
- https://bitcoinfaucet.uo1.net

The faucet will send a small amount of TestNet BTC to your address.

### 3. Wait for sync

Go to **Balance**. Check that:
- **Sync status**: Synced
- **Block height** is increasing

The first sync downloads all TestNet3 headers — this can take 5–20 minutes depending on your connection.

### 4. Check balance

Go to **Balance**. Your TestNet BTC will appear after at least 1 confirmation (~10 min).

### 5. Send a transaction

Go to **Transactions** → fill in a destination TestNet address and amount in satoshis.
Click **Send Transaction**. The tx hash appears after broadcast.

A Kafka event is published → consumer persists the transaction → appears in the list.

### 6. Sign a message

Go to **Sign / Verify** → enter the wallet address (P2PKH, must be from this wallet) and a message.
Click **Sign Message**. Copy the base64 signature.

### 7. Verify a message

In the same page, enter the address, message, and signature. Click **Verify Signature**.

---

## REST API Examples

### Generate address
```bash
curl -X POST http://localhost:8080/api/wallet/address
```

### Get balance
```bash
curl http://localhost:8080/api/wallet/balance
```

### List addresses
```bash
curl http://localhost:8080/api/wallet/addresses
```

### Send transaction
```bash
curl -X POST http://localhost:8080/api/transactions/send \
  -H "Content-Type: application/json" \
  -d '{"toAddress":"mwR1a1LGBfMn2JtdqDjf6eaD4xoFXkrRUT","amountSatoshis":10000}'
```

### List transactions
```bash
curl http://localhost:8080/api/transactions
```

### Sign message
```bash
curl -X POST http://localhost:8080/api/messages/sign \
  -H "Content-Type: application/json" \
  -d '{"address":"mXXX...","message":"Hello Bitcoin"}'
```

### Verify message
```bash
curl -X POST http://localhost:8080/api/messages/verify \
  -H "Content-Type: application/json" \
  -d '{"address":"mXXX...","message":"Hello Bitcoin","signature":"IIXX...base64..."}'
```

### Health check
```bash
curl http://localhost:8080/actuator/health
```

---

## API Error Format

All errors return structured JSON:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Invalid Bitcoin Address",
  "message": "Adresse Bitcoin invalide pour TestNet3: xyz",
  "path": "/api/transactions/send"
}
```

---

## Local Development (without Docker)

### Backend
```bash
cd backend
mvn spring-boot:run
```
Requires: Java 17, MongoDB on localhost:27017, Kafka on localhost:9092.

### Frontend
```bash
cd frontend
npm install
npm start   # uses proxy.conf.json → proxies /api to localhost:8080
```

---

## Troubleshooting

### Kafka connection error on backend startup
Backend waits for Kafka health check before starting.
If it times out, check:
```bash
docker compose logs kafka
```
Kafka can take 30 seconds to become ready. Try:
```bash
docker compose restart backend
```

### MongoDB connection error
```bash
docker compose logs mongodb
```
Check `bitcoin-network` is created:
```bash
docker network ls | grep bitcoin
```

### BitcoinJ SPV sync delay
The first sync downloads all block headers since TestNet3 genesis (2011).
This is normal. Watch the logs:
```bash
docker compose logs -f backend | grep -i "chain\|sync\|block"
```

### Wallet has zero balance
1. Confirm the wallet is synced (Balance page shows `Synced`).
2. Confirm the faucet transaction has at least 1 confirmation.
3. Check that you used the correct generated address.
4. TestNet reorgs are common — wait for 3+ confirmations.

### Insufficient funds error
The wallet must have more satoshis than the amount + transaction fee.
BitcoinJ adds the fee automatically. Minimum useful amount: ~10000 satoshis.

### Angular cannot call backend
In Docker: Nginx proxies `/api` to `backend:8080`. Check:
```bash
docker compose logs frontend
```
In local dev: make sure `proxy.conf.json` is used (`npm start`, not `ng serve`).

### Docker port already in use
Stop any existing services using ports 4200, 8080, 27017, 9092, 2181:
```bash
docker compose down
sudo lsof -i :8080
```

---

## Security Notes

> **This is an educational TestNet project.**

1. **TestNet coins have no real value.** Do not confuse TestNet and MainNet.
2. **Do not use this code as-is for MainNet Bitcoin.** It lacks hardening required for real funds.
3. Private keys are managed exclusively by BitcoinJ wallet files (not in MongoDB, not in API responses).
4. Wallet files are stored in a Docker volume. Back them up if you have testnet funds.
5. For production systems:
   - Use an HSM or a secrets manager (HashiCorp Vault, AWS KMS, Azure Key Vault).
   - Enable TLS on all communication.
   - Use authentication and authorization on all API endpoints.
   - Monitor wallet activity.
   - Implement proper backup/recovery procedures for wallet files.

---

## Known Limitations

| Limitation | Reason |
|---|---|
| Per-address balance not available | SPV mode only tracks wallet-level UTXOs, not individual address balances. A block explorer API would be needed. |
| First sync is slow | TestNet3 has millions of block headers. Checkpoints could speed this up. |
| Message signing only with P2PKH | The standard Bitcoin message signing protocol is defined for legacy (P2PKH) addresses only. |
| Single-node Kafka | replication-factor=1. Not fault-tolerant. Suitable for development only. |
| No authentication | API endpoints are open. Not suitable for public deployment. |
| TestNet instability | TestNet3 can have large reorgs and long gaps between blocks. |

---

## Possible Future Improvements

- Add block explorer integration for per-address balance
- Add BIP39 mnemonic display/import for wallet recovery
- Add wallet backup/export endpoint
- Add JWT authentication on backend
- Add HTTPS/TLS support
- Use BitcoinJ checkpoints file to speed up initial sync
- Add WebSocket for real-time balance/transaction updates
- Add multi-wallet support
- Migrate to TestNet4 (newer, more stable test network)
- Add Prometheus metrics via Actuator

---

## Project Structure

```
client-web-blockchain-bitcoin-testnet/
├── docker-compose.yml
├── README.md
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/bitcoin/testnet/
│       ├── BitcoinTestnetApplication.java
│       ├── config/           # BitcoinProperties, CorsConfig, KafkaConfig
│       ├── controller/       # WalletController, TransactionController, MessageController
│       ├── document/         # WalletAddress, TransactionDocument (MongoDB)
│       ├── dto/              # Request and response DTOs
│       ├── event/            # TransactionEvent (Kafka payload)
│       ├── exception/        # Custom exceptions + GlobalExceptionHandler
│       ├── kafka/            # TransactionProducer, TransactionConsumer
│       ├── repository/       # Spring Data MongoDB repositories
│       └── service/          # BitcoinService, WalletService, TransactionService
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    ├── proxy.conf.json
    ├── angular.json
    ├── package.json
    └── src/app/
        ├── components/       # dashboard, address, balance, transaction, message
        ├── models/           # TypeScript interfaces
        └── services/         # wallet, transaction, message Angular services
```
