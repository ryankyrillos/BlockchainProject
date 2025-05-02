package blockchain;

import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides secure storage for wallet private keys
 * Uses password-based encryption to protect keys
 */
public class KeyStorage {
    
    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final byte[] SALT = "BlockchainSalt123".getBytes(); // Fixed salt for simplicity
    
    /**
     * Save a private key to a file with password-based encryption
     * 
     * @param privateKey The private key to save
     * @param password The password to encrypt with
     * @param filePath The path to save the encrypted key
     * @return true if successful, false otherwise
     */
    public static boolean savePrivateKey(PrivateKey privateKey, String password, String filePath) {
        try {
            // Convert private key to bytes
            byte[] privateKeyBytes = privateKey.getEncoded();
            
            // Generate a secret key from the password
            SecretKey secretKey = generateSecretKey(password);
            
            // Encrypt the private key
            byte[] encryptedKey = encrypt(privateKeyBytes, secretKey);
            
            // Save the encrypted key to a file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(encryptedKey);
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Error saving private key: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load a private key from a file with password-based decryption
     * 
     * @param password The password to decrypt with
     * @param filePath The path to load the encrypted key from
     * @return The private key, or null if loading fails
     */
    public static PrivateKey loadPrivateKey(String password, String filePath) {
        try {
            // Read the encrypted key from the file
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Error loading private key: " + filePath + " (The system cannot find the file specified)");
                return null;
            }
            
            byte[] encryptedKey = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(encryptedKey);
            }
            
            // Generate a secret key from the password
            SecretKey secretKey = generateSecretKey(password);
            
            // Decrypt the private key
            byte[] decryptedKey = decrypt(encryptedKey, secretKey);
            
            // Convert bytes back to a private key
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            System.out.println("Error loading private key: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate a secret key from a password using PBKDF2
     * 
     * @param password The password to derive the key from
     * @return The secret key
     */
    private static SecretKey generateSecretKey(String password) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), SALT, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    /**
     * Encrypt data with a secret key
     * 
     * @param data The data to encrypt
     * @param key The secret key to encrypt with
     * @return The encrypted data
     */
    private static byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
    
    /**
     * Decrypt data with a secret key
     * 
     * @param data The data to decrypt
     * @param key The secret key to decrypt with
     * @return The decrypted data
     */
    private static byte[] decrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
