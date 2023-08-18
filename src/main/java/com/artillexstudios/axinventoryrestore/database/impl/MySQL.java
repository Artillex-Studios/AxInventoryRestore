package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axinventoryrestore.api.events.InventoryBackupEvent;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class MySQL implements Database {
    private final HikariConfig hConfig = new HikariConfig();
    private HikariDataSource dataSource;

    @Override
    public String getType() {
        return "MySQL";
    }

    @Override
    public void setup() {

        hConfig.setPoolName("rivalsauction-pool");

        hConfig.setMaximumPoolSize(CONFIG.getInt("database.pool.maximum-pool-size"));
        hConfig.setMinimumIdle(CONFIG.getInt("database.pool.minimum-idle"));
        hConfig.setMaxLifetime(CONFIG.getInt("database.pool.maximum-lifetime"));
        hConfig.setKeepaliveTime(CONFIG.getInt("database.pool.keepalive-time"));
        hConfig.setConnectionTimeout(CONFIG.getInt("database.pool.connection-timeout"));

        hConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hConfig.setJdbcUrl("jdbc:mysql://" + CONFIG.getString("database.address") + ":"+ CONFIG.getString("database.port") +"/" + CONFIG.getString("database.database"));
        hConfig.addDataSourceProperty("user", CONFIG.getString("database.username"));
        hConfig.addDataSourceProperty("password", CONFIG.getString("database.password"));

        dataSource = new com.zaxxer.hikari.HikariDataSource(hConfig);

        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `axinventoryrestore_data` ( `player` VARCHAR(36) NOT NULL, `reason` VARCHAR(64) NOT NULL, `location` VARCHAR(256) NOT NULL, `inventory` VARCHAR NOT NULL, `time` INT NOT NULL, `cause` VARCHAR(64) NOT NULL );";

        try (Connection connection = dataSource.getConnection(); PreparedStatement stmt = connection.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void saveInventory(@NotNull Player player, @NotNull String reason, @NotNull String cause) {

        final InventoryBackupEvent inventoryBackupEvent = new InventoryBackupEvent(player, reason, cause);
        Bukkit.getPluginManager().callEvent(inventoryBackupEvent);
        if (inventoryBackupEvent.isCancelled()) return;

        boolean isEmpty = true;

        for (ItemStack it : player.getInventory().getContents()) {
            if (it == null) continue;

            isEmpty = false;
        }

        if (isEmpty) return;

        final String ex = "INSERT INTO `axinventoryrestore_data`(`player`, `reason`, `location`, `inventory`, `time`, `cause`) VALUES (?,?,?,?,?,?);";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, reason);
            stmt.setString(3, LocationUtils.serializeLocation(player.getLocation(), true));
            stmt.setString(4, SerializationUtils.invToBase64(player.getInventory().getContents()));
            stmt.setLong(5, System.currentTimeMillis());
            stmt.setString(6, cause);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<BackupData> getDeathsByType(@NotNull OfflinePlayer player, @NotNull String reason) {
        final ArrayList<BackupData> backups = new ArrayList<>();

        String ex = "SELECT * FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ? ORDER BY `time` DESC;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, reason);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    backups.add(new BackupData(Bukkit.getOfflinePlayer(rs.getString(1)),
                            rs.getString(2),
                            LocationUtils.deserializeLocation(rs.getString(3)),
                            SerializationUtils.invFromBase64(rs.getString(4)),
                            rs.getLong(5),
                            rs.getString(6)));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return backups;
    }

    @Override
    public ArrayList<String> getDeathReasons(@NotNull OfflinePlayer player) {
        final ArrayList<String> reasons = new ArrayList<>();

        String ex = "SELECT DISTINCT `reason` FROM `axinventoryrestore_data` WHERE `player` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reasons.add(rs.getString("reason"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reasons;
    }

    @Override
    public int getDeathsSizeType(@NotNull OfflinePlayer player, @NotNull String reason) {

        String ex = "SELECT COUNT(*) FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, reason);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void cleanup() {
        String ex = "DELETE FROM `axinventoryrestore_data` WHERE `time` < ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setLong(1, System.currentTimeMillis() - (86_400_000L * CONFIG.getLong("cleanup-after-days")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disable() {
        try {
            dataSource.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
