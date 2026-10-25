package com.sks.model;

public enum OutboxStatus {
    TRANSFER_CREATED,
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}
