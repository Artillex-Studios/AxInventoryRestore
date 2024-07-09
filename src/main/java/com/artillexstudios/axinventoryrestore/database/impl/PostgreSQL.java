package com.artillexstudios.axinventoryrestore.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class PostgreSQL extends Base {
    private HikariDataSource dataSource;

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getType() {
        return "PostgreSQL";
    }

    @Override
    public void setup() {
        final HikariConfig hConfig = new HikariConfig();

        hConfig.setPoolName("axinventoryrestore-pool");

        hConfig.setMaximumPoolSize(CONFIG.getInt("database.pool.maximum-pool-size"));
        hConfig.setMinimumIdle(CONFIG.getInt("database.pool.minimum-idle"));
        hConfig.setMaxLifetime(CONFIG.getInt("database.pool.maximum-lifetime"));
        hConfig.setKeepaliveTime(CONFIG.getInt("database.pool.keepalive-time"));
        hConfig.setConnectionTimeout(CONFIG.getInt("database.pool.connection-timeout"));

        hConfig.setDriverClassName("org.postgresql.Driver");
        hConfig.setJdbcUrl("jdbc:postgresql://" + CONFIG.getString("database.address") + ":"+ CONFIG.getString("database.port") +"/" + CONFIG.getString("database.database"));
        hConfig.addDataSourceProperty("user", CONFIG.getString("database.username"));
        hConfig.addDataSourceProperty("password", CONFIG.getString("database.password"));

        this.dataSource = new HikariDataSource(hConfig);
        super.setup();
    }

    @Override
    public void disable() {
        super.disable();
        try {
            dataSource.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
