package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.api.events.InventoryBackupEvent;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class H2 implements Database {
    private Connection conn;

    @Override
    public String getType() {
        return "H2";
    }

    @Override
    public void setup() {

        try {
            Class.forName("org.h2.Driver");
            this.conn = DriverManager.getConnection(String.format("jdbc:h2:./%s/data", AxInventoryRestore.getInstance().getDataFolder()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `axinventoryrestore_data` ( `player` VARCHAR(36) NOT NULL, `reason` VARCHAR(64) NOT NULL, `location` VARCHAR(256) NOT NULL, `inventory` VARCHAR NOT NULL, `time` BIGINT NOT NULL, `cause` VARCHAR(64) NOT NULL );";

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
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

        try (PreparedStatement stmt = conn.prepareStatement(ex)) {
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
        try (PreparedStatement stmt = conn.prepareStatement(ex)) {
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
        try (PreparedStatement stmt = conn.prepareStatement(ex)) {
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
    public void cleanup() {
        String ex = "DELETE FROM `axinventoryrestore_data` WHERE `time` < ?;";
        try (PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setLong(1, System.currentTimeMillis() - (86_400_000L * AxInventoryRestore.CONFIG.getLong("cleanup-after-days")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
