CREATE DATABASE banking_db;

USE banking_db;

CREATE TABLE account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_account_balance
        CHECK (balance >= 0),

    CONSTRAINT chk_account_type
        CHECK (account_type IN ('SAVINGS', 'CURRENT')),

    CONSTRAINT chk_account_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
);

INSERT INTO account (
    account_number,
    customer_id,
    account_holder_name,
    account_type,
    balance
)
VALUES
    ('ACC10001', 101, 'Rahul Sharma', 'SAVINGS', 50000.0000),
    ('ACC10002', 102, 'Priya Singh', 'CURRENT', 125000.0000),
    ('ACC10003', 103, 'Amit Kumar', 'SAVINGS', 25000.0000);