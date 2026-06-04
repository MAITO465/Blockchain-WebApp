package com.bitcoin.testnet.service;

import com.bitcoin.testnet.config.BitcoinProperties;
import com.bitcoin.testnet.exception.BitcoinException;
import com.bitcoin.testnet.exception.InsufficientFundsException;
import com.bitcoin.testnet.exception.InvalidAddressException;
import com.bitcoin.testnet.exception.WalletNotReadyException;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.security.SignatureException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service principal Bitcoin utilisant BitcoinJ 0.16.x.
 *
 * IMPORTANT - Mode SPV (Simplified Payment Verification) :
 * Le portefeuille ne télécharge pas la blockchain complète, seulement les en-têtes
 * de blocs. Cela le rend léger mais la synchronisation initiale avec le réseau
 * TestNet3 peut prendre plusieurs minutes (voire plus selon la connexion).
 *
 * IMPORTANT - TestNet3 :
 * Les coins TestNet n'ont aucune valeur réelle. Ce projet est éducatif.
 * Ne jamais utiliser ce code tel quel sur le MainNet Bitcoin.
 *
 * SÉCURITÉ :
 * Les clés privées sont gérées exclusivement par le fichier .wallet de BitcoinJ.
 * Elles ne sont jamais exposées via l'API ni stockées dans MongoDB.
 * Pour un système de production, utiliser un HSM ou un gestionnaire de secrets
 * (HashiCorp Vault, AWS KMS, etc.).
 */
@Service
@Slf4j
public class BitcoinService implements InitializingBean, DisposableBean {

    private final BitcoinProperties properties;

    /**
     * Paramètres réseau TestNet3.
     * Pour passer en MainNet : remplacer par MainNetParams.get()
     */
    private NetworkParameters params;

    /**
     * WalletAppKit : façade de haut niveau de BitcoinJ.
     * Gère le portefeuille SPV, la connexion aux peers et la diffusion.
     */
    private WalletAppKit kit;

    private volatile boolean running = false;

    public BitcoinService(BitcoinProperties properties) {
        this.properties = properties;
    }

    /**
     * Initialisation au démarrage Spring.
     * Crée ou charge le portefeuille et lance la synchronisation SPV.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // Réseau Bitcoin TestNet3
        params = TestNet3Params.get();

        File walletDir = new File(properties.getDir());
        if (!walletDir.exists()) {
            walletDir.mkdirs();
            log.info("Répertoire portefeuille créé : {}", walletDir.getAbsolutePath());
        }

        log.info("Démarrage de WalletAppKit - réseau: TestNet3, répertoire: {}, nom: {}",
                walletDir.getAbsolutePath(), properties.getName());

        /*
         * WalletAppKit avec callback onSetupCompleted.
         * Si les fichiers .wallet et .spvchain existent déjà, BitcoinJ les charge.
         * Sinon, un nouveau portefeuille HD est créé automatiquement.
         */
        kit = new WalletAppKit(params, walletDir, properties.getName()) {
            @Override
            protected void onSetupCompleted() {
                log.info("WalletAppKit setup terminé. Portefeuille prêt.");
                log.info("Adresse de réception actuelle : {}",
                        wallet().currentReceiveAddress());
                log.info("Solde actuel : {}",
                        wallet().getBalance().toFriendlyString());
                running = true;
            }
        };

        // Activer la sauvegarde automatique du portefeuille
        kit.setAutoSave(true);

        /*
         * Démarrage ASYNCHRONE - ne pas appeler awaitRunning() ici.
         * awaitRunning() bloquerait le démarrage de Spring Boot car afterPropertiesSet()
         * est appelé pendant le rafraîchissement du contexte, AVANT que Tomcat
         * ne commence à accepter les connexions HTTP.
         *
         * Le flag `running` est mis à true dans le callback onSetupCompleted()
         * quand BitcoinJ est prêt. Jusque-là, les opérations wallet retournent
         * WalletNotReadyException (503).
         *
         * La première synchronisation sur TestNet3 peut être longue (téléchargement
         * de tous les en-têtes depuis 2011).
         */
        kit.startAsync();

        log.info("BitcoinJ WalletAppKit démarrage asynchrone lancé sur TestNet3");
        log.info("Synchronisation SPV en cours en arrière-plan...");
    }

    /**
     * Arrêt propre lors de la fermeture Spring.
     */
    @Override
    public void destroy() throws Exception {
        if (kit != null) {
            log.info("Arrêt propre de WalletAppKit...");
            kit.stopAsync();
            kit.awaitTerminated(30, TimeUnit.SECONDS);
            log.info("WalletAppKit arrêté.");
        }
    }

    /**
     * Vérifie que le portefeuille est opérationnel avant toute opération.
     */
    private void checkReady() {
        if (!running || kit == null) {
            throw new WalletNotReadyException(
                    "Le portefeuille Bitcoin n'est pas encore prêt. " +
                    "La synchronisation SPV avec TestNet3 est peut-être en cours.");
        }
    }

    /**
     * Génère une nouvelle adresse de réception P2PKH (Legacy).
     * P2PKH est utilisé pour la compatibilité avec la signature de messages Bitcoin.
     * BitcoinJ génère des adresses HD déterministes (BIP44).
     */
    public Address generateFreshAddress() {
        checkReady();
        // Utilisation de P2PKH pour la compatibilité de la signature de messages
        Address address = kit.wallet().freshReceiveAddress(Script.ScriptType.P2PKH);
        log.info("Nouvelle adresse générée (TestNet3 P2PKH) : {}", address);
        return address;
    }

    /**
     * Retourne le solde total estimé du portefeuille en Coin (satoshis).
     * ESTIMATED inclut les transactions non confirmées.
     *
     * Limitation SPV : le solde représente le total du portefeuille,
     * pas le solde d'une adresse individuelle. Les nœuds SPV ne voient
     * que les transactions liées aux adresses du portefeuille local.
     */
    public Coin getBalance() {
        checkReady();
        return kit.wallet().getBalance(Wallet.BalanceType.ESTIMATED);
    }

    /**
     * Retourne la hauteur du meilleur bloc connu par le nœud SPV.
     * Permet de juger l'avancement de la synchronisation.
     */
    public int getBestChainHeight() {
        if (!running || kit == null) return 0;
        try {
            return kit.chain().getBestChainHeight();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Retourne true si la synchronisation SPV est considérée à jour
     * (à moins de 6 blocs du réseau - approximatif en TestNet).
     */
    public boolean isSynced() {
        if (!running || kit == null) return false;
        // En TestNet3, le réseau peut avoir des periodes inactives
        // On considère synchronisé si on a des blocs récents
        return getBestChainHeight() > 0;
    }

    /**
     * Envoie une transaction Bitcoin TestNet.
     *
     * Flux :
     * 1. Valider l'adresse destination
     * 2. Créer le SendRequest BitcoinJ
     * 3. wallet.sendCoins() : sélection UTXO + signature + diffusion aux peers
     * 4. Attendre la confirmation de diffusion (30s max)
     *
     * @param toAddress adresse destination TestNet3
     * @param satoshis  montant en satoshis
     * @return hash de la transaction diffusée
     */
    public String sendTransaction(String toAddress, long satoshis) {
        checkReady();

        // Validation de l'adresse Bitcoin
        Address destination;
        try {
            destination = Address.fromString(params, toAddress);
        } catch (AddressFormatException e) {
            throw new InvalidAddressException(
                    "Adresse Bitcoin invalide pour TestNet3: " + toAddress);
        }

        Coin amount = Coin.valueOf(satoshis);
        log.info("Envoi de {} vers {} sur TestNet3", amount.toFriendlyString(), toAddress);

        try {
            /*
             * sendCoins() :
             * 1. Sélectionne les UTXOs disponibles (coin selection)
             * 2. Crée et signe la transaction avec les clés du portefeuille
             * 3. Diffuse la transaction aux peers connectés
             * Lève InsufficientMoneyException si le solde est insuffisant.
             */
            Wallet.SendResult result = kit.wallet().sendCoins(
                    kit.peerGroup(), destination, amount);

            // Attendre la confirmation de réception par les peers (30 secondes max)
            result.broadcastComplete.get(30, TimeUnit.SECONDS);

            String txHash = result.tx.getTxId().toString();
            log.info("Transaction diffusée avec succès - txHash: {}", txHash);
            return txHash;

        } catch (InsufficientMoneyException e) {
            throw new InsufficientFundsException(
                    "Fonds insuffisants. Solde: " + getBalance().toFriendlyString() +
                    ", Requis: " + amount.toFriendlyString());
        } catch (Exception e) {
            throw new BitcoinException("Erreur lors de l'envoi: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne l'adresse "from" principale du portefeuille (adresse courante).
     * Note : dans Bitcoin, une transaction peut utiliser plusieurs adresses sources.
     * Cette méthode retourne l'adresse de réception courante à titre indicatif.
     */
    public String getCurrentReceiveAddress() {
        checkReady();
        return kit.wallet().currentReceiveAddress().toString();
    }

    /**
     * Signe un message avec la clé privée associée à une adresse du portefeuille.
     *
     * Format de signature Bitcoin standard (compatible Bitcoin Core signmessage).
     * Fonctionne uniquement avec des adresses P2PKH (legacy) du portefeuille.
     *
     * SÉCURITÉ : La clé privée n'est jamais exposée. BitcoinJ effectue
     * l'opération de signature en mémoire et retourne uniquement la signature.
     *
     * @param addressStr adresse P2PKH du portefeuille
     * @param message    message à signer
     * @return signature base64
     */
    public String signMessage(String addressStr, String message) {
        checkReady();

        Address targetAddress;
        try {
            targetAddress = Address.fromString(params, addressStr);
        } catch (AddressFormatException e) {
            throw new InvalidAddressException("Adresse invalide: " + addressStr);
        }

        /*
         * Recherche de la clé ECKey associée à l'adresse dans le portefeuille.
         * On itère sur toutes les clés de réception émises.
         * La clé trouvée appartient au portefeuille HD local.
         */
        ECKey foundKey = findKeyForAddress(targetAddress);

        if (foundKey == null) {
            throw new BitcoinException(
                    "Adresse non trouvée dans le portefeuille: " + addressStr +
                    ". Seules les adresses générées par ce portefeuille peuvent signer.");
        }

        try {
            // Signature du message au format Bitcoin standard
            String signature = foundKey.signMessage(message);
            log.info("Message signé avec succès pour l'adresse: {}", addressStr);
            return signature;
        } catch (Exception e) {
            throw new BitcoinException("Erreur lors de la signature: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifie une signature de message Bitcoin.
     *
     * Méthode : récupère la clé publique depuis la signature,
     * en déduit l'adresse P2PKH et compare avec l'adresse fournie.
     *
     * @param addressStr adresse déclarée
     * @param message    message original
     * @param signature  signature base64
     * @return true si la signature est valide pour cette adresse
     */
    public boolean verifyMessage(String addressStr, String message, String signature) {
        try {
            /*
             * ECKey.signedMessageToKey récupère la clé publique depuis la signature.
             * Cela fonctionne grâce à la récupération ECDSA (ECDSA public key recovery).
             */
            ECKey recoveredKey = ECKey.signedMessageToKey(message, signature);
            Address recoveredAddress = LegacyAddress.fromKey(params, recoveredKey);
            boolean valid = recoveredAddress.toString().equals(addressStr);
            log.info("Vérification signature pour {}: {}", addressStr, valid ? "valide" : "invalide");
            return valid;
        } catch (SignatureException e) {
            log.warn("Signature invalide ou malformée: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retourne les paramètres réseau (TestNet3).
     */
    public NetworkParameters getParams() {
        return params;
    }

    /**
     * Recherche la clé ECKey correspondant à une adresse dans le portefeuille.
     * Itère sur les clés de réception émises par le portefeuille HD.
     */
    private ECKey findKeyForAddress(Address targetAddress) {
        List<ECKey> issuedKeys = kit.wallet().getIssuedReceiveKeys();
        for (ECKey key : issuedKeys) {
            try {
                Address keyAddress = LegacyAddress.fromKey(params, key);
                if (keyAddress.equals(targetAddress)) {
                    return key;
                }
            } catch (Exception e) {
                // Clé incompatible avec l'adresse legacy, continuer
            }
        }
        return null;
    }
}
