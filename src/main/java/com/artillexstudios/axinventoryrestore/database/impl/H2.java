package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class H2 extends Base {
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
        return "H2";
    }

    @Override
    public void setup() {
        final HikariConfig hConfig = new HikariConfig();
        hConfig.setPoolName("axinventoryrestore-pool");
        hConfig.setDriverClassName("org.h2.Driver");
        hConfig.setJdbcUrl("jdbc:h2:./" + AxInventoryRestore.getInstance().getDataFolder() + "/data;mode=MySQL");

        this.dataSource = new HikariDataSource(hConfig);
        super.setup();
    }

    @Override
    public void disable() {
        final String sql = "SHUTDOWN COMPACT;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
