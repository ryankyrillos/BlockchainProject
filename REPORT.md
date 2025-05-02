# Blockchain Implementation Report

## Project Overview

This project extends a Java blockchain implementation with additional features to create a more robust and secure blockchain system. The implementation includes both small and advanced features as required by the project specifications.

## Features Implemented

### Small Features

#### 1. Transaction Fees
- **Implementation**: Added a fee field to the Transaction class that allows users to specify a fee when creating a transaction.
- **Code Location**: `Transaction.java` (lines 16, 26, 58)
- **Functionality**: When a user creates a transaction, they can specify a fee amount. This fee is deducted from the sender's balance along with the transaction amount.
- **Testing**: Tested in `Driver.java` by creating transactions with different fee amounts and verifying that the fees are correctly deducted from the sender and added to the miner's reward.

#### 2. Coinbase Transactions
- **Implementation**: Created a special type of transaction (coinbase transaction) that is used to reward miners.
- **Code Location**: `Transaction.java` (lines 140-149), `Block.java` (lines 37-46)
- **Functionality**: When a block is mined, a coinbase transaction is created that rewards the miner with a fixed block reward plus all transaction fees from the block.
- **Testing**: Tested in `Driver.java` by mining blocks and verifying that the miner receives the correct reward.

### Advanced Features

#### 1. Secure Key Storage
- **Implementation**: Created a KeyStorage class that provides secure storage for wallet private keys using password-based encryption.
- **Code Location**: `KeyStorage.java`
- **Functionality**: Private keys are encrypted using AES encryption with a key derived from a user-provided password. The encrypted keys are stored in files and can be loaded and decrypted using the same password.
- **Testing**: Tested in `Driver.java` by creating a wallet with secure key storage, saving the keys to a file, and loading them back.

#### 2. Transaction Fees (Advanced Implementation)
- **Implementation**: Extended the basic transaction fee system to include fee collection by miners and fee-based transaction prioritization.
- **Code Location**: `Block.java` (lines 37-46), `Transaction.java` (lines 58-63)
- **Functionality**: Miners collect all transaction fees from a block in addition to the block reward. Transactions with higher fees are prioritized.
- **Testing**: Tested in `Driver.java` by creating transactions with different fee amounts and verifying that the fees are correctly collected by miners.

#### 3. Defense Against Double Spending
- **Implementation**: Created a TransactionValidator class that validates transactions to prevent double spending.
- **Code Location**: `TransactionValidator.java`
- **Functionality**: The validator tracks spent outputs and rejects transactions that attempt to spend already spent outputs. It also validates transactions within blocks to prevent double spending.
- **Testing**: Tested in `Driver.java` by attempting to create transactions that spend the same outputs and verifying that the second transaction is rejected.

## Implementation Details

### Blockchain Structure
The blockchain is implemented as a chain of blocks, each containing:
- A hash of the previous block
- A timestamp
- A nonce for mining
- A merkle root of the transactions
- A list of transactions

### Transaction Structure
Transactions include:
- Sender's public key
- Recipient's public key
- Amount to transfer
- Transaction fee
- Digital signature
- Inputs (references to previous transaction outputs)
- Outputs (new UTXOs created by this transaction)

### Mining Process
Mining is implemented using a Proof of Work algorithm with adjustable difficulty:
1. Create a new block with pending transactions
2. Calculate the merkle root of the transactions
3. Add a coinbase transaction to reward the miner
4. Increment the nonce until a hash with the required number of leading zeros is found
5. Add the block to the blockchain

### Security Measures
- Digital signatures using ECDSA to verify transaction authenticity
- SHA-256 hashing for block and transaction hashes
- Password-based encryption for key storage
- Double spending prevention through transaction validation
- Automatic difficulty adjustment to maintain consistent block times

## Testing

The implementation was tested using the Driver class, which:
1. Creates wallets for testing
2. Tests secure key storage by saving and loading keys
3. Creates a genesis block with an initial transaction
4. Tests transaction creation and validation
5. Tests mining with transaction fees
6. Tests double spending prevention
7. Tests automatic difficulty adjustment
8. Validates the blockchain

## Challenges and Solutions

### Challenge 1: Double Spending Prevention
- **Challenge**: Preventing users from spending the same outputs multiple times.
- **Solution**: Implemented a TransactionValidator class that tracks spent outputs and rejects transactions that attempt to spend already spent outputs.

### Challenge 2: Secure Key Storage
- **Challenge**: Securely storing private keys to prevent theft.
- **Solution**: Implemented password-based encryption using AES to encrypt private keys before storing them in files.

### Challenge 3: Transaction Fee Collection
- **Challenge**: Ensuring miners collect all transaction fees from a block.
- **Solution**: Modified the mining process to calculate the total fees from all transactions in a block and add them to the coinbase transaction.

## Conclusion

This blockchain implementation successfully extends the basic blockchain with additional features to create a more robust and secure system. The implementation includes transaction fees, secure key storage, and defense mechanisms against double spending, meeting all the requirements specified in the project.

## Future Improvements

1. **Client-Server Architecture**: Implement a client-server architecture to allow multiple users to interact with the blockchain.
2. **GUI Interface**: Create a graphical user interface for easier interaction with the blockchain.
3. **Smart Contracts**: Add support for smart contracts to enable more complex transactions.
4. **Improved Consensus Mechanism**: Implement a more sophisticated consensus mechanism like Proof of Stake.
5. **Network Implementation**: Add networking capabilities to allow nodes to communicate and synchronize the blockchain.
