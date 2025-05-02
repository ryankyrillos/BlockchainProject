package blockchain;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * TransactionValidator class for the blockchain
 * Provides validation for transactions to prevent double spending
 */
public class TransactionValidator {
    
    // Mempool to store unconfirmed transactions
    private static ArrayList<Transaction> mempool = new ArrayList<Transaction>();
    
    // Map to track spent outputs (for double spending prevention)
    private static HashMap<String, String> spentOutputs = new HashMap<String, String>();
    
    // Number of confirmations required for a transaction to be considered final
    private static final int CONFIRMATION_THRESHOLD = 6;
    
    /**
     * Add a transaction to the mempool
     * 
     * @param transaction The transaction to add
     * @return true if the transaction was added, false otherwise
     */
    public static boolean addToMempool(Transaction transaction) {
        // Skip coinbase transactions
        if (transaction.isCoinbase()) {
            return false;
        }
        
        // Check for double spending
        for (TransactionInput input : transaction.inputs) {
            if (spentOutputs.containsKey(input.transactionOutputId)) {
                System.out.println("Double spending attempt detected in mempool!");
                return false;
            }
        }
        
        // Add transaction to mempool
        mempool.add(transaction);
        
        // Mark outputs as spent
        for (TransactionInput input : transaction.inputs) {
            spentOutputs.put(input.transactionOutputId, transaction.transactionId);
        }
        
        return true;
    }
    
    /**
     * Validate a transaction to prevent double spending
     * 
     * @param transaction The transaction to validate
     * @return true if the transaction is valid, false otherwise
     */
    public static boolean validateTransaction(Transaction transaction) {
        // Skip coinbase transactions
        if (transaction.isCoinbase()) {
            return true;
        }
        
        // Check for double spending
        for (TransactionInput input : transaction.inputs) {
            if (spentOutputs.containsKey(input.transactionOutputId)) {
                String spendingTx = spentOutputs.get(input.transactionOutputId);
                if (!spendingTx.equals(transaction.transactionId)) {
                    System.out.println("Double spending attempt detected!");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Update the mempool when a new block is added to the blockchain
     * 
     * @param blockchain The current blockchain
     */
    public static void updateMempool(ArrayList<Block> blockchain) {
        // Remove confirmed transactions from mempool
        for (int i = 0; i < blockchain.size(); i++) {
            Block block = blockchain.get(i);
            for (Transaction tx : block.transactions) {
                mempool.remove(tx);
                
                // If the block is deep enough in the chain, remove from spent outputs
                if (blockchain.size() - i > CONFIRMATION_THRESHOLD) {
                    for (TransactionInput input : tx.inputs) {
                        if (spentOutputs.containsKey(input.transactionOutputId) && 
                            spentOutputs.get(input.transactionOutputId).equals(tx.transactionId)) {
                            spentOutputs.remove(input.transactionOutputId);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Validate all transactions in a block
     * 
     * @param block The block to validate
     * @return true if all transactions are valid, false otherwise
     */
    public static boolean validateBlockTransactions(Block block) {
        // Track outputs spent within this block to prevent double spending in the same block
        HashMap<String, String> blockSpentOutputs = new HashMap<String, String>();
        
        for (Transaction tx : block.transactions) {
            // Skip coinbase transactions
            if (tx.isCoinbase()) {
                continue;
            }
            
            // Check for double spending within the block
            for (TransactionInput input : tx.inputs) {
                if (blockSpentOutputs.containsKey(input.transactionOutputId)) {
                    System.out.println("Double spending detected within block!");
                    return false;
                }
                blockSpentOutputs.put(input.transactionOutputId, tx.transactionId);
            }
            
            // Validate the transaction
            if (!validateTransaction(tx)) {
                return false;
            }
        }
        
        return true;
    }
}
