package com.sks.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TransactionIdGenerator {

    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public TransactionIdGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public String generateTransactionId() {

        LocalDate today = LocalDate.now();

        // Atomically create or increment today's counter.
        jdbcTemplate.update("""
            INSERT INTO transaction_sequence (sequence_date, last_number)
            VALUES (?, LAST_INSERT_ID(1))
            ON DUPLICATE KEY UPDATE
                last_number = LAST_INSERT_ID(last_number + 1)
            """, java.sql.Date.valueOf(today));

        // Retrieve the number allocated by this database connection.
        Long sequence = jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Long.class
        );

        return "TXN-"
                + today.format(DATE_FORMAT)
                + "-"
                + String.format("%05d", sequence);
    }
}