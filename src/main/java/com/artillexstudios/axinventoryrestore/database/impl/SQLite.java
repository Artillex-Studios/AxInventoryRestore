package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.api.events.InventoryBackupEvent;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.h2.jdbc.JdbcConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class SQLite implements Database {
    private Connection conn;

    @Override
    public String getType() {
        return "SQLite";
    }

    @Override
    public void setup() {

        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s/data.db", AxInventoryRestore.getInstance().getDataFolder()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        boolean migrating = false;
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "AXINVENTORYRESTORE_BACKUPS", null)) {
            try (ResultSet rs2 = conn.getMetaData().getTables(null, null, "AXINVENTORYRESTORE_DATA", null)) {
                if (!rs.next() && rs2.next()) {
                    migrating = true;

                    Bukkit.getConsoleSender().sendMessage(ColorUtils.format("&#FF6600[AxInventoryRestore] Your database is outdated, we will start migrating it.."));

                    final String ex = "ALTER TABLE `axinventoryrestore_data` RENAME TO `axinventoryrestore_temp`;";

                    try (PreparedStatement stmt = conn.prepareStatement(ex)) {
                        stmt.executeUpdate();
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `axinventoryrestore_data` ( `player` VARCHAR(36) NOT NULL, `reason` VARCHAR(64) NOT NULL, `location` VARCHAR(256) NOT NULL, `id` INTEGER PRIMARY KEY, `time` BIGINT NOT NULL, `cause` VARCHAR(512) );";

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String CREATE_TABLE2 = "CREATE TABLE IF NOT EXISTS `axinventoryrestore_backups` ( `id` INTEGER PRIMARY KEY, `inventory` VARCHAR NOT NULL );";

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE2)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        if (!migrating) return;

        final String ex = "SELECT * FROM `axinventoryrestore_temp`;";
        int mcount = 0;

        try (PreparedStatement stmt = conn.prepareStatement(ex)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    final String ex2 = "INSERT INTO `axinventoryrestore_backups`(`inventory`) VALUES (?);";
                    final String ex3 = "INSERT INTO `axinventoryrestore_data`(`player`, `reason`, `location`, `id`, `time`, `cause`) VALUES (?,?,?,?,?,?);";

                    try (PreparedStatement stmt2 = conn.prepareStatement(ex2, Statement.RETURN_GENERATED_KEYS)) {
                        stmt2.setString(1, rs.getString(4));
                        stmt2.executeUpdate();

                        try (ResultSet rs2 = stmt2.getGeneratedKeys(); PreparedStatement stmt3 = conn.prepareStatement(ex3)) {
                            rs2.next();

                            stmt3.setString(1, rs.getString(1));
                            stmt3.setString(2, rs.getString(2));
                            stmt3.setString(3, rs.getString(3));
                            stmt3.setInt(4, rs2.getInt(1));
                            stmt3.setLong(5, System.currentTimeMillis());
                            stmt3.setString(6, rs.getString(6).equals("---") ? null : rs.getString(6));
                            stmt3.executeUpdate();
                            Bukkit.getConsoleSender().sendMessage(ColorUtils.format("&#FF6600[AxInventoryRestore] Migrating database.. " + mcount));
                            mcount++;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String ex2 = "DROP TABLE `axinventoryrestore_temp`;";

        try (PreparedStatement stmt = conn.prepareStatement(ex2)) {
            stmt.executeUpdate();
            Bukkit.getConsoleSender().sendMessage(ColorUtils.format("&#FF6600[AxInventoryRestore] Migrated database!"));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String ex3 = "VACUUM;";

        try (PreparedStatement stmt = conn.prepareStatement(ex3)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        setup();
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

        try (PreparedStatement stmt = conn.prepareStatement(ex, Statement.RETURN_GENERATED_KEYS)) {
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
    public ArrayList<BackupData> getDeathsByType(@NotNull OfflinePlayer player, @NotNull String reason) {
        final ArrayList<BackupData> backups = new ArrayList<>();

        // long time = System.currentTimeMillis();

        final String ex = "SELECT * FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ? ORDER BY `time` DESC;";
        final String ex2 = "SELECT `inventory` FROM `axinventoryrestore_backups` WHERE `id` = ?";

        try (PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, reason);

            try (ResultSet rs = stmt.executeQuery()) {
                // System.out.println((System.currentTimeMillis() - time) + " - SELECT * FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ? ORDER BY `time` DESC;");

                while (rs.next()) {

                    try (PreparedStatement stmt2 = conn.prepareStatement(ex2)) {
                        stmt2.setInt(1, rs.getInt(4));

                        try (ResultSet rs2 = stmt2.executeQuery()) {
                            // System.out.println((System.currentTimeMillis() - time) + " - SELECT `inventory` FROM `axinventoryrestore_backups` WHERE `id` = ?");
                            rs2.next();
                            backups.add(new BackupData(Bukkit.getOfflinePlayer(rs.getString(1)),
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
    public int getDeathsSizeType(@NotNull OfflinePlayer player, @NotNull String reason) {

        // long time = System.currentTimeMillis();

        String ex = "SELECT COUNT(`id`) FROM `axinventoryrestore_data` WHERE `player` = ? AND `reason` = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());
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

    @Override
    public ArrayList<String> getDeathReasons(@NotNull OfflinePlayer player) {
        final ArrayList<String> reasons = new ArrayList<>();

        // long time = System.currentTimeMillis();

        String ex = "SELECT DISTINCT `reason` FROM `axinventoryrestore_data` WHERE `player` = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setString(1, player.getUniqueId().toString());

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
    public void cleanup() {
        String ex = "DELETE FROM `axinventoryrestore_data` WHERE `time` < ?;";
        try (PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setLong(1, System.currentTimeMillis() - (86_400_000L * AxInventoryRestore.CONFIG.getLong("cleanup-after-days")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disable() {
        try {
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
