package blockchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Wallet class for the blockchain
 */
public class Wallet {
    
    public PrivateKey privateKey;
    public PublicKey publicKey;
    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    
    // Default constructor
    public Wallet(){
        generateKeyPair();
    }
    
    // Constructor with key loading from secure storage
    public Wallet(String password, String keyFilePath) {
        // Try to load keys from storage
        PrivateKey loadedKey = KeyStorage.loadPrivateKey(password, keyFilePath);
        
        if (loadedKey != null) {
            // Key loaded successfully, but we need to generate a new key pair
            // since we can't derive the public key easily
            System.out.println("Private key successfully loaded from: " + keyFilePath);
            System.out.println("Private key loaded, but generating a new key pair for simplicity.");
            generateKeyPair();
        } else {
            // If loading fails, generate a new key pair
            System.out.println("No existing key found. Generating a new wallet.");
            generateKeyPair();
            
            // Save the new key pair
            saveKeys(password, keyFilePath);
        }
    }
    
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: Driver.UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
                total += UTXO.value ;
            }
        }
        return total;
    }
    
    public Transaction sendFunds(PublicKey Newrecipient,float value ) {
        return sendFunds(Newrecipient, value, 0.1f); // Default fee of 0.1
    }
    
    public Transaction sendFunds(PublicKey Newrecipient, float value, float fee) {
        if(getBalance() < (value + fee)) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            System.out.println("Required: " + (value + fee) + ", Available: " + getBalance());
            return null;
        }
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
        
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > (value + fee)) break;
        }
        
        Transaction newTransaction = new Transaction(publicKey, Newrecipient, value, fee, inputs);
        newTransaction.generateSignature(privateKey);
        
        // Gathers transaction inputs (Making sure they are unspent):
        for(TransactionInput i : newTransaction.inputs) {
            i.UTXO = Driver.UTXOs.get(i.transactionOutputId);
        }
        
        // Generate transaction outputs:
        // Get value of inputs and calculate the left over:
        float leftOver = newTransaction.getInputsValue() - value - fee;
        newTransaction.transactionId = newTransaction.calculateHash();
        // Send value to recipient
        newTransaction.outputs.add(new TransactionOutput( newTransaction.recipient,
                                    newTransaction.value, newTransaction.transactionId));
        // Send the left over 'change' back to sender
        newTransaction.outputs.add(new TransactionOutput( newTransaction.sender,
                                    leftOver, newTransaction.transactionId));
        
        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        
        return newTransaction;
    }
    
    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            // 256 bytes provides an acceptable security level
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Saves the wallet's private key to a file using secure encryption
     *
     * @param password The password to encrypt the key with
     * @param filePath The path to save the encrypted key
     * @return true if successful, false otherwise
     */
    public boolean saveKeys(String password, String filePath) {
        try {
            boolean success = KeyStorage.savePrivateKey(privateKey, password, filePath);
            if (success) {
                System.out.println("Wallet keys securely saved to: " + filePath);
            }
            return success;
        } catch (Exception e) {
            System.out.println("Error saving wallet keys: " + e.getMessage());
            return false;
        }
    }
}
