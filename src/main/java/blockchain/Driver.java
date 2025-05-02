package blockchain;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Driver class to test the blockchain implementation
 */
public class Driver {
    
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
    public static float minimumTransaction = 0.1f;
    public static int difficulty = 4;
    
    // Wallets
    public static Wallet walletA;
    public static Wallet walletB;
    public static Wallet walletC;
    public static Wallet minerWallet;
    
    // Genesis transaction
    public static Transaction genesisTransaction;
    
    public static void main(String[] args) {
        // Setup Bouncey castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        // Create wallets
        walletA = new Wallet();
        walletB = new Wallet();
        walletC = new Wallet();
        minerWallet = new Wallet();
        
        // Test secure key storage
        System.out.println("Testing secure key storage...");
        Wallet secureWallet = new Wallet("password123", "secure_wallet.dat");
        System.out.println("Secure wallet created with public key: " + StringUtil.getStringFromKey(secureWallet.publicKey));
        
        // Create genesis transaction
        System.out.println("Creating and processing genesis transaction...");
        genesisTransaction = Transaction.createCoinbaseTransaction(walletA.publicKey, 100f, "0");
        genesisTransaction.transactionId = "0";
        
        // Create genesis block
        Block genesisBlock = new Block("0");
        genesisBlock.addTransaction(genesisTransaction);
        addBlock(genesisBlock);
        
        System.out.println("WalletA balance: " + walletA.getBalance());
        
        // Test transaction with fees
        System.out.println("\nWalletA sending 40 to WalletB with fee...");
        Block block1 = new Block(blockchain.get(blockchain.size()-1).hash);
        // Send 40 coins from walletA to walletB with a fee of 1
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f, 1f));
        addBlock(block1);
        
        System.out.println("WalletA balance: " + walletA.getBalance());
        System.out.println("WalletB balance: " + walletB.getBalance());
        System.out.println("Miner balance: " + minerWallet.getBalance());
        
        // Test double spending prevention
        System.out.println("\nTesting double spending prevention...");
        Block block2 = new Block(blockchain.get(blockchain.size()-1).hash);
        // Try to send more than walletA has (should fail)
        Transaction tx1 = walletA.sendFunds(walletC.publicKey, 60f, 1f);
        if (tx1 != null) {
            block2.addTransaction(tx1);
        }
        
        // Try to send the same funds again (should fail due to double spending)
        Transaction tx2 = walletA.sendFunds(walletC.publicKey, 30f, 1f);
        if (tx2 != null) {
            block2.addTransaction(tx2);
        }
        
        addBlock(block2);
        
        System.out.println("WalletA balance: " + walletA.getBalance());
        System.out.println("WalletB balance: " + walletB.getBalance());
        System.out.println("WalletC balance: " + walletC.getBalance());
        System.out.println("Miner balance: " + minerWallet.getBalance());
        
        // Test automatic difficulty adjustment
        System.out.println("\nTesting automatic difficulty adjustment...");
        System.out.println("Current difficulty: " + difficulty);
        
        // Mine several blocks to trigger difficulty adjustment
        for (int i = 0; i < 10; i++) {
            Block block = new Block(blockchain.get(blockchain.size()-1).hash);
            block.addTransaction(walletB.sendFunds(walletC.publicKey, 1f, 0.1f));
            addBlock(block);
            System.out.println("Block " + i + " mined with difficulty: " + difficulty);
        }
        
        System.out.println("Final difficulty after adjustment: " + difficulty);
        
        // Validate the blockchain
        System.out.println("\nBlockchain validation: " + isChainValid());
    }
    
    public static void addBlock(Block newBlock) {
        // Use the miner's wallet to receive the block reward
        newBlock.mineBlock(difficulty, minerWallet.publicKey);
        blockchain.add(newBlock);
        
        // Adjust difficulty if needed
        if (blockchain.size() % 10 == 0 && blockchain.size() > 1) {
            adjustDifficulty();
        }
    }
    
    // Adjust mining difficulty based on the time it took to mine the last 10 blocks
    public static void adjustDifficulty() {
        if (blockchain.size() <= 10) {
            return; // Not enough blocks to adjust
        }
        
        Block latestBlock = blockchain.get(blockchain.size() - 1);
        Block adjustmentBlock = blockchain.get(blockchain.size() - 10);
        
        // Calculate the time it took to mine the last 10 blocks
        long timeExpected = 10000 * 10; // 10 seconds per block
        long timeActual = latestBlock.getMineTime() - adjustmentBlock.getMineTime();
        
        System.out.println("Difficulty adjustment: Expected time: " + timeExpected + "ms, Actual time: " + timeActual + "ms");
        
        // If mining is too fast, increase difficulty
        if (timeActual < timeExpected / 4) {
            difficulty++;
            System.out.println("Mining too fast. Increasing difficulty to: " + difficulty);
        } 
        // If mining is too slow, decrease difficulty (but not below 1)
        else if (timeActual > timeExpected * 4) {
            if (difficulty > 1) {
                difficulty--;
                System.out.println("Mining too slow. Decreasing difficulty to: " + difficulty);
            }
        }
    }
    
    // Validate the blockchain
    public static Boolean isChainValid() {
        Block currentBlock; 
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        
        // Loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            
            // Compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal");
                return false;
            }
            
            // Compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            
            // Check if hash is solved
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        
        return true;
    }
}
