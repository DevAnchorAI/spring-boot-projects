package com.sks.model;

public enum TransferStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    COMPENSATION_PENDING,
    COMPENSATED,
    RECONCILIATION_REQUIRED
}