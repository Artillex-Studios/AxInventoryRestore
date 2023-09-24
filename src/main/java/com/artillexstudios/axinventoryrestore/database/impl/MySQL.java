package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
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
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

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

        hConfig.setPoolName("axinventoryrestore-pool");

        hConfig.setMaximumPoolSize(CONFIG.getInt("database.pool.maximum-pool-size"));
        hConfig.setMinimumIdle(CONFIG.getInt("database.pool.minimum-idle"));
        hConfig.setMaxLifetime(CONFIG.getInt("database.pool.maximum-lifetime"));
        hConfig.setKeepaliveTime(CONFIG.getInt("database.pool.keepalive-time"));
        hConfig.setConnectionTimeout(CONFIG.getInt("database.pool.connection-timeout"));

        hConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hConfig.setJdbcUrl("jdbc:mysql://" + CONFIG.getString("database.address") + ":"+ CONFIG.getString("database.port") +"/" + CONFIG.getString("database.database"));
        hConfig.addDataSourceProperty("user", CONFIG.getString("database.username"));
        hConfig.addDataSourceProperty("password", CONFIG.getString("database.password"));

        dataSource = new HikariDataSource(hConfig);

        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `axinventoryrestore_data` ( `player` VARCHAR(36) NOT NULL, `reason` VARCHAR(64) NOT NULL, `location` VARCHAR(256) NOT NULL, `id` INTEGER PRIMARY KEY, `time` BIGINT NOT NULL, `cause` VARCHAR(512) );";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String CREATE_TABLE2 = "CREATE TABLE IF NOT EXISTS `axinventoryrestore_backups` ( `id` INTEGER PRIMARY KEY AUTO_INCREMENT, `inventory` TEXT NOT NULL );";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE2)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String CREATE_TABLE3 = "CREATE TABLE IF NOT EXISTS `axinventoryrestore_uuids` ( `uuid` VARCHAR(36), `name` VARCHAR(64), PRIMARY KEY (`uuid`) );";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE3)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void saveInventory(@NotNull Player player, @NotNull String reason, @Nullable String cause) {

        final InventoryBackupEvent inventoryBackupEvent = new InventoryBackupEvent(player, reason, cause);
        Bukkit.getPluginManager().callEvent(inventoryBackupEvent);
        if (inventoryBackupEvent.isCancelled()) return;

        boolean isEmpty = true;

        for (ItemStack it : player.getInventory().getContents()) {
            if (it == null) continue;

            isEmpty = false;
        }

        if (isEmpty) return;

        final String ex = "INSERT INTO `axinventoryrestore_backups`(`inventory`) VALUES (?);";
        final String ex2 = "INSERT INTO `axinventoryrestore_data`(`player`, `reason`, `location`, `id`, `time`, `cause`) VALUES (?,?,?,?,?,?);";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, SerializationUtils.invToBase64(player.getInventory().getContents()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys(); PreparedStatement stmt2 = conn.prepareStatement(ex2)) {
                rs.next();

                stmt2.setString(1, player.getUniqueId().toString());
                stmt2.setString(2, reason);
                stmt2.setString(3, LocationUtils.serializeLocation(player.getLocation(), true));
                stmt2.setInt(4, rs.getInt(1));
                stmt2.setLong(5, System.currentTimeMillis());
                stmt2.setString(6, cause);
                stmt2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<BackupData> getDeathsByType(@NotNull UUID uuid, @NotNull String reason) {
        final ArrayList<BackupData> backups = new ArrayList<>();

        // long time = System.currentTimeMillis();

        String ex = "SELECT * FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ? ORDER BY `time` DESC;";
        final String ex2 = "SELECT `inventory` FROM `axinventoryrestore_backups` WHERE `id` = ?";

        if (reason.equals("ALL"))
            ex = "SELECT * FROM `axinventoryrestore_data` WHERE `player` = ? ORDER BY `time` DESC;";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, uuid.toString());
            if (!reason.equals("ALL"))
                stmt.setString(2, reason);

            try (ResultSet rs = stmt.executeQuery()) {
                // System.out.println((System.currentTimeMillis() - time) + " - SELECT * FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ? ORDER BY `time` DESC;");

                while (rs.next()) {

                    try (PreparedStatement stmt2 = conn.prepareStatement(ex2)) {
                        stmt2.setInt(1, rs.getInt(4));

                        try (ResultSet rs2 = stmt2.executeQuery()) {
                            // System.out.println((System.currentTimeMillis() - time) + " - SELECT `inventory` FROM `axinventoryrestore_backups` WHERE `id` = ?");
                            rs2.next();

                            backups.add(new BackupData(UUID.fromString(rs.getString(1)),
                                    rs.getString(2),
                                    LocationUtils.deserializeLocation(rs.getString(3)),
                                    SerializationUtils.invFromBase64(rs2.getString(1)),
                                    rs.getLong(5),
                                    rs.getString(6)));
                        }
                    }
                }

//                // System.out.println((System.currentTimeMillis() - time) + " - SELECT * FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ? ORDER BY `time` DESC;");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return backups;
    }

    @Override
    public int getDeathsSizeType(@NotNull UUID uuid, @NotNull String reason) {

        // long time = System.currentTimeMillis();

        String ex = "SELECT COUNT(`id`) FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ?;";

        if (reason.equals("ALL"))
            ex = "SELECT COUNT(`id`) FROM `axinventoryrestore_data` WHERE `player` = ?;";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, uuid.toString());
            if (!reason.equals("ALL"))
                stmt.setString(2, reason);

            try (ResultSet rs = stmt.executeQuery()) {
                // System.out.println((System.currentTimeMillis() - time) + " - SELECT COUNT(`id`) FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ?;");
                if (rs.next())
                    return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Nullable
    @Override
    public UUID getUUID(@NotNull String player) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

        if (offlinePlayer == null) {

            String ex = "SELECT `uuid` FROM `axinventoryrestore_uuids` WHERE `player` = ? LIMIT 1;";
            try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
                stmt.setString(1, player);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return UUID.fromString(rs.getString(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return null;
        }

        return offlinePlayer.getUniqueId();
    }

    @Override
    public ArrayList<String> getDeathReasons(@NotNull UUID uuid) {
        final ArrayList<String> reasons = new ArrayList<>();

        // long time = System.currentTimeMillis();

        String ex = "SELECT DISTINCT `reason` FROM `axinventoryrestore_data` WHERE `player` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                // System.out.println((System.currentTimeMillis() - time) + " - SELECT DISTINCT `reason` FROM `axinventoryrestore_data` WHERE `player` = ?;");
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
    public void join(@NotNull Player player) {
        String ex = "INSERT INTO `axinventoryrestore_uuids` (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setString(3, player.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        String ex = "DELETE FROM `axinventoryrestore_data` WHERE `time` < ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setLong(1, System.currentTimeMillis() - (86_400_000L * AxInventoryRestore.CONFIG.getLong("cleanup-after-days")));
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
