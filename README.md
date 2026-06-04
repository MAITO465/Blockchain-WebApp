# Blockchain Web App - Bitcoin TestNet3 Client

Une application full-stack (Spring Boot + Angular + MongoDB + Kafka) permettant d'interagir avec le réseau Bitcoin TestNet3. Ce projet démontre l'intégration de portefeuilles SPV (Simplified Payment Verification) via BitcoinJ, une architecture orientée événements avec Kafka, et une sécurisation robuste avec JWT (JSON Web Tokens).

## 🚀 Fonctionnalités Principales

- **Génération d'Adresses** : Créez de nouvelles adresses de réception Bitcoin TestNet3 (P2PKH) spécifiques à chaque utilisateur.
- **Consultation de Solde** : Visualisez le solde total du portefeuille SPV.
- **Envoi de Transactions** : Transférez des fonds TestNet vers d'autres adresses (avec isolation stricte des fonds par utilisateur grâce à un `CoinSelector` personnalisé).
- **Signature et Vérification de Messages** : Prouvez la propriété d'une adresse en signant un message, et vérifiez des signatures.
- **Sécurité et Authentification** : Inscription et connexion sécurisées basées sur JWT. Les données (adresses et transactions) sont cloisonnées par utilisateur.
- **Architecture Événementielle** : Les transactions génèrent des événements Kafka qui sont ensuite consommés de manière asynchrone pour la persistance en base de données.

## 🛠️ Stack Technique

### Backend
- **Java 17** / **Spring Boot 3.2.x**
- **Spring Security & JJWT** (Authentification JWT sans état)
- **BitcoinJ 0.16.x** (Nœud SPV TestNet3, gestion du portefeuille cryptographique)
- **Spring Data MongoDB** (Persistance des adresses et transactions)
- **Spring Kafka** (Publish/Subscribe des événements de transaction)

### Frontend
- **Angular 17** (Standalone Components)
- **RxJS** / **HttpClient**
- Intercepteurs HTTP pour l'injection du token JWT
- Guards pour la protection des routes

### Infrastructure
- **Docker & Docker Compose**
- **MongoDB** (Base de données NoSQL)
- **Apache Kafka & Zookeeper** (Broker de messages)

## 🏗️ Architecture et Flux de Données (Isolation par Utilisateur)

1. **Authentification** : Lorsqu'un utilisateur se connecte, un token JWT est généré. Ce token doit être inclus dans l'en-tête `Authorization: Bearer <token>` de chaque requête API subséquente.
2. **Génération d'adresse** : Lorsqu'un utilisateur génère une adresse, son `userId` (extrait du JWT) est enregistré dans MongoDB en l'associant à l'adresse.
3. **Isolation des transactions (Coin Selection)** : Lors d'un envoi de fonds, le backend vérifie quelles adresses appartiennent à l'utilisateur courant, et configure le `CoinSelector` de BitcoinJ pour n'utiliser *que* les UTXOs (Unspent Transaction Outputs) liés à ces adresses spécifiques. Il est impossible de dépenser les fonds d'un autre utilisateur.

## 🧪 Guide de Test Pas à Pas

Suivez ces étapes pour tester complètement le projet en local.

### Étape 1 : Prérequis

Assurez-vous d'avoir installé sur votre machine :
- **Docker** et **Docker Compose**
- **Git**

### Étape 2 : Lancement de l'Infrastructure et des Applications

Ouvrez un terminal à la racine du projet et exécutez :

```bash
docker compose up --build
```

Cela va :
1. Démarrer Zookeeper et Kafka.
2. Démarrer MongoDB.
3. Compiler et démarrer le Backend Spring Boot (port `8080`). *Note : Le backend commencera à synchroniser la blockchain TestNet3 en arrière-plan. Cela peut prendre quelques minutes.*
4. Compiler et démarrer le Frontend Angular (port `4200`).

### Étape 3 : Inscription et Connexion

1. Ouvrez votre navigateur et allez sur **http://localhost:4200**.
2. Vous serez redirigé vers la page de **Connexion**. Cliquez sur le lien pour vous **Inscrire**.
3. Créez un compte (ex: nom d'utilisateur `alice`, mot de passe `password123`).
4. Une fois le compte créé, connectez-vous avec ces identifiants. Vous accéderez au tableau de bord.

### Étape 4 : Génération d'une Adresse et Faucet

1. Dans le menu latéral, allez sur **Addresses**.
2. Cliquez sur **Générer une nouvelle adresse**. L'adresse générée sera liée à votre compte (`alice`).
3. Copiez cette adresse.
4. Comme vous êtes sur le TestNet3, vous avez besoin de fausse monnaie. Allez sur un Faucet public, par exemple :
   - [Coindeno Testnet Faucet](https://coindeno.com/faucet/bitcoin-testnet)
   - [Bitcoinfaucet.uo1.net](https://bitcoinfaucet.uo1.net/)
5. Collez votre adresse et demandez des fonds.

### Étape 5 : Consultation du Solde

1. Dans l'application, allez sur **Balance**.
2. Vous pourrez voir l'état de synchronisation du nœud SPV (qui tourne dans le backend).
3. Une fois la synchronisation suffisamment avancée (le backend rattrape la hauteur du réseau), vous verrez les fonds envoyés par le Faucet apparaître dans votre solde local.

### Étape 6 : Test de l'Isolation et Envoi de Transactions

1. Pour prouver l'isolation des données, ouvrez un navigateur en navigation privée, allez sur **http://localhost:4200**, et créez un deuxième compte (`bob`).
2. Allez dans **Addresses** avec le compte `bob`. Vous verrez que la liste est vide ! Les adresses d'`alice` ne sont pas visibles.
3. Retournez sur la session d'`alice`. Allez dans **Transactions**.
4. Entrez l'adresse de destination (vous pouvez générer une adresse avec `bob` et l'utiliser ici) et un montant en Satoshis (ex: `10000`).
5. Cliquez sur **Envoyer**.
   - Le backend vérifiera que les UTXOs utilisés appartiennent bien à `alice`.
   - La transaction sera envoyée au réseau Bitcoin TestNet3.
   - Un événement Kafka sera publié.
   - Le consommateur Kafka sauvegardera la transaction dans MongoDB, associée au compte d'`alice`.
6. Vérifiez que la transaction apparaît dans l'historique des transactions.

### Étape 7 : Signature et Vérification de Message

1. Allez dans la section **Sign / Verify**.
2. Choisissez l'une de vos adresses générées et tapez un message (ex: `Ceci est une preuve`).
3. Cliquez sur **Signer le message**. Une signature cryptographique Base64 vous sera retournée.
4. Basculez sur l'onglet **Vérifier une signature**.
5. Entrez l'adresse, le message exact, et la signature Base64.
6. Le système validera que le message a bien été signé par le propriétaire de l'adresse (sans jamais exposer la clé privée).

## 🛑 Arrêt de l'application

Pour arrêter proprement les conteneurs et libérer les ports, exécutez dans le terminal :

```bash
docker compose down
```

Les données (blockchain SPV, base de données MongoDB, données Kafka) sont conservées dans les volumes Docker définis (ex: `mongodb_data`, `wallet_data`).
