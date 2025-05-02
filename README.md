# Java Blockchain Implementation

This project is an extended implementation of a blockchain system in Java, featuring transaction fees, secure key storage, and defense mechanisms against double spending.

## Features

### Small Features
1. **Transaction Fees**: Implemented a fee system where users can include fees with their transactions, which are collected by miners.
2. **Coinbase Transactions**: Block rewards and transaction fees are collected by miners through coinbase transactions.

### Advanced Features
1. **Secure Key Storage**: Private keys are securely stored using password-based encryption.
2. **Transaction Fees**: Advanced implementation of transaction fees with proper fee handling and collection.
3. **Defense Mechanisms**: Implemented protection against double spending through transaction validation.

## Project Structure

- `src/main/java/blockchain/`: Contains all the Java classes for the blockchain implementation
  - `Block.java`: Represents a block in the blockchain
  - `Driver.java`: Main class to run and test the blockchain
  - `KeyStorage.java`: Handles secure storage of private keys
  - `StringUtil.java`: Utility class for hashing and cryptographic functions
  - `Transaction.java`: Represents a transaction in the blockchain
  - `TransactionInput.java`: Represents an input to a transaction
  - `TransactionOutput.java`: Represents an output from a transaction
  - `TransactionValidator.java`: Validates transactions to prevent double spending
  - `Wallet.java`: Manages user wallets and keys

## How to Run

1. Compile the project:
```
.\compile.bat
```

2. Run the blockchain:
```
.\run.bat
```

## Requirements

- Java Development Kit (JDK) 8 or higher
- Bouncy Castle Cryptography Library (included in the lib folder)

## Implementation Details

### Blockchain
The blockchain is implemented as a chain of blocks, each containing a list of transactions. Each block includes:
- A hash of the previous block
- A timestamp
- A nonce for mining
- A merkle root of the transactions
- A list of transactions

### Transactions
Transactions include:
- Sender's public key
- Recipient's public key
- Amount to transfer
- Transaction fee
- Digital signature
- Inputs (references to previous transaction outputs)
- Outputs (new UTXOs created by this transaction)

### Mining
Mining is implemented using a Proof of Work algorithm with adjustable difficulty. Miners are rewarded with:
- Block reward (fixed amount)
- Transaction fees from all transactions in the block

### Security
- Digital signatures using ECDSA
- SHA-256 hashing
- Password-based encryption for key storage
- Double spending prevention

## License
This project is licensed under the MIT License - see the LICENSE file for details.
