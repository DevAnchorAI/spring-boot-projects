package com.spring.ai.debug;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceDebugger {

    private final DataSource dataSource;

    public DataSourceDebugger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void check() {

        HikariDataSource hikari = (HikariDataSource) dataSource;

        System.out.println("=================================");
        System.out.println("JDBC URL  = " + hikari.getJdbcUrl());
        System.out.println("USERNAME  = " + hikari.getUsername());
        System.out.println("=================================");

        try (var connection = hikari.getConnection()) {

            System.out.println("DATABASE CONNECTION = SUCCESS");
            System.out.println("DATABASE = " +
                    connection.getMetaData().getDatabaseProductName());
            System.out.println("=================================");
        } catch (Exception e) {

            System.out.println("DATABASE CONNECTION = FAILED");
            e.printStackTrace();
        }
    }
}