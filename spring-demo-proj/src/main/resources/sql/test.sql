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



CREATE TABLE banking_db.transfer (
    transaction_id VARCHAR(100) PRIMARY KEY,
    from_account VARCHAR(50) NOT NULL,
    to_account VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE banking_db.outbox_event (
    event_id VARCHAR(100) PRIMARY KEY,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL
);

CREATE TABLE banking_db.transaction_sequence (
    sequence_date DATE PRIMARY KEY,
    last_number BIGINT NOT NULL
);

ALTER TABLE banking_db.transfer
ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

ALTER TABLE banking_db.outbox_event
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0,
    ADD COLUMN last_error VARCHAR(1000) NULL;


--create index
create index idx_transfer_from_account on banking_db.transfer(from_account);
create index idx_transfer_to_account on banking_db.transfer(to_account);

create index idx_outbox_status_created on banking_db.outbox_event(status, created_at);
create index idx_outbox_aggregate on banking_db.outbox_event(aggregate_id);