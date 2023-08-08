package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SQLite implements Database {
    private final String url = "jdbc:sqlite:" + AxInventoryRestore.getInstance().getDataFolder() + "/data.db";

    @Override
    public String getType() {
        return "SQLite";
    }

    @Override
    public void setup() {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

        final String CREATE_TABLE = """
                CREATE TABLE IF NOT EXISTS `axinventoryrestore_data` (
                	`player` VARCHAR(36) NOT NULL,
                	`reason` VARCHAR(64) NOT NULL,
                	`location` VARCHAR(256) NOT NULL,
                	`inventory` VARCHAR NOT NULL,
                	`time` INT NOT NULL
                );""";

        try (Connection connection = DriverManager.getConnection(url); PreparedStatement stmt = connection.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void saveInventory(@NotNull Player player, @NotNull SaveReason reason) {
        String ex = """
              INSERT INTO `axinventoryrestore_data`(`player`, `reason`, `location`, `inventory`, `time`) VALUES (?,?,?,?,?)
               """;
        try (Connection conn = DriverManager.getConnection(url); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, reason.toString());
            stmt.setString(3, LocationUtils.serializeLocation(player.getLocation(), true));
            stmt.setString(4, SerializationUtils.invToBase64(player.getInventory().getContents()));
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<BackupData> getDeathsByType(@NotNull OfflinePlayer player, @NotNull SaveReason reason) {
        final ArrayList<BackupData> backups = new ArrayList<>();

        String ex = """
              SELECT * FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ?;
               """;
        try (Connection conn = DriverManager.getConnection(url); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, reason.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    backups.add(new BackupData(Bukkit.getOfflinePlayer(rs.getString(1)),
                            SaveReason.valueOf(rs.getString(2)),
                            LocationUtils.deserializeLocation(rs.getString(3)),
                            SerializationUtils.invFromBase64(rs.getString(4)),
                            rs.getLong(5)));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return backups;
    }
}
