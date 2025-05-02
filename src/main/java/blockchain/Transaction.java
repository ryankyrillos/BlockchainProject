package blockchain;

import java.security.*;
import java.util.ArrayList;

/**
 * Transaction class for the blockchain
 */
public class Transaction {
    public String transactionId; // Contains a hash of transaction
    public PublicKey sender; // Senders address/public key
    public PublicKey recipient; // Recipients address/public key
    public float value; // Contains the amount to be transferred
    public float fee; // Transaction fee
    public byte[] signature; // This is to prevent anybody else from spending funds in our wallet

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0; // A rough count of how many transactions have been generated

    // Constructor
    public Transaction(PublicKey from, PublicKey to, float value, float fee, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.fee = fee;
        this.inputs = inputs;
    }

    // Calculate the transaction hash (which will be used as its Id)
    public String calculateHash() {
        sequence++; // Increase the sequence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(recipient) +
                Float.toString(value) +
                Float.toString(fee) +
                sequence
        );
    }

    // Signs all the data we don't wish to be tampered with
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(recipient) +
                Float.toString(value) +
                Float.toString(fee);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // Verifies the data we signed hasn't been tampered with
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(recipient) +
                Float.toString(value) +
                Float.toString(fee);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    // Returns true if new transaction could be created
    public boolean processTransaction() {
        // Verify signature
        if(verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // Gather transaction inputs (Make sure they are unspent)
        for(TransactionInput i : inputs) {
            i.UTXO = Driver.UTXOs.get(i.transactionOutputId);
        }

        // Check if transaction is valid
        if(getInputsValue() < Driver.minimumTransaction) {
            System.out.println("#Transaction Inputs too small: " + getInputsValue());
            return false;
        }

        // Generate transaction outputs
        float leftOver = getInputsValue() - value - fee; // Get value of inputs then the left over change
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); // Send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // Send the left over 'change' back to sender

        // Add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            Driver.UTXOs.put(o.id, o);
        }

        // Remove transaction inputs from UTXO lists as spent
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; // If Transaction can't be found skip it
            Driver.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // Returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; // If Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    // Returns sum of outputs
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    // Create a coinbase transaction (no inputs, just outputs)
    public static Transaction createCoinbaseTransaction(PublicKey recipient, float value, String blockHash) {
        Transaction tx = new Transaction(null, recipient, value, 0, new ArrayList<TransactionInput>());
        tx.transactionId = blockHash; // Use block hash as transaction id
        tx.outputs.add(new TransactionOutput(recipient, value, tx.transactionId));

        // Add to UTXOs
        Driver.UTXOs.put(tx.outputs.get(0).id, tx.outputs.get(0));

        return tx;
    }

    // Check if this is a coinbase transaction
    public boolean isCoinbase() {
        return (this.inputs.size() == 0 && this.fee == 0);
    }
}
