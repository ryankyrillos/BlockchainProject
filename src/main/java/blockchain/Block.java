package blockchain;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;

/**
 * Block class for the blockchain
 */
public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    public long timeStamp;
    public int nonce;
    private long mineTime; // Time it took to mine the block

    // Block Constructor
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    // Calculate new hash based on blocks contents
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                Long.toString(timeStamp) +
                Integer.toString(nonce) +
                merkleRoot
        );
        return calculatedhash;
    }

    // Increases nonce value until hash target is reached
    public void mineBlock(int difficulty, PublicKey minerAddress) {
        long startTime = System.currentTimeMillis();
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty); // Create a string with difficulty * "0"

        // Add coinbase transaction (block reward + fees)
        float fees = calculateFees();
        Transaction coinbaseTransaction = Transaction.createCoinbaseTransaction(minerAddress, 50f, this.hash);
        coinbaseTransaction.outputs.get(0).value += fees; // Add fees to the block reward
        transactions.add(0, coinbaseTransaction); // Add at the beginning

        // Recalculate merkle root with coinbase transaction
        merkleRoot = StringUtil.getMerkleRoot(transactions);

        while(!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }

        long endTime = System.currentTimeMillis();
        mineTime = endTime - startTime;

        System.out.println("Block Mined!!! : " + hash);
        System.out.println("Block Reward: 50.0, Total Fees: " + fees);
        System.out.println("Mining time: " + mineTime + " ms");
    }

    // Calculate total fees from all transactions in the block
    private float calculateFees() {
        float totalFees = 0;
        for(Transaction transaction : transactions) {
            if(!transaction.isCoinbase()) {
                totalFees += transaction.fee;
            }
        }
        return totalFees;
    }

    // Add transactions to this block
    public boolean addTransaction(Transaction transaction) {
        // Process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;

        if((!"0".equals(previousHash))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }

        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

    // Get the time it took to mine this block
    public long getMineTime() {
        return mineTime;
    }
}
