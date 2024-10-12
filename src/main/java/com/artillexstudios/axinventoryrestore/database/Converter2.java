package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.database.impl.Base;
import com.artillexstudios.axinventoryrestore.database.impl.H2;
import com.artillexstudios.axinventoryrestore.database.impl.MySQL;
import com.artillexstudios.axinventoryrestore.database.impl.PostgreSQL;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.SQLUtils;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Converter2 {
    private final Base base;

    public Converter2(Base base) {
        this.base = base;

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Migrating database... Don't stop the server while it's running!"));
        int success = 0;
        if (convertUsers()) success++;
        if (convertBackups()) success++;

        if (success == 2) {
            dropOldTables();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FF00[AxInventoryRestore] Successful conversion!"));
        } else {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxInventoryRestore] Something went wrong while converting!"));
        }
    }

    public boolean convertUsers() {
        if (!SQLUtils.tableExists(base.getConnection(), "axinventoryrestore_uuids")) return true;

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converting users..."));

        final String sql = "SELECT * FROM axinventoryrestore_uuids;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                final String sql2 = "INSERT INTO axir_users(uuid, name) VALUES (?,?);";
                try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                    int processed = 0;
                    while (rs.next()) {
                        stmt2.setString(1, rs.getString(1));
                        stmt2.setString(2, rs.getString(2));
                        if (processed % 100 == 0) {
                            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converted: " + processed));
                            stmt2.executeBatch();
                        }
                        processed++;
                        stmt2.addBatch();
                    }
                    stmt2.executeBatch();
                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converted users!"));
                }
            }

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean convertBackups() {
        if (!SQLUtils.tableExists(base.getConnection(), "axinventoryrestore_data")) return true;

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converting backups... This might take a while!"));

        final String sql = "SELECT * FROM axinventoryrestore_data;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                final String sql2 = "INSERT INTO axir_backups(userId, reasonId, world, x, y, z, inventory, time, cause) VALUES (?,?,?,?,?,?,?,?,?);";
                try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                    int processed = 0;
                    while (rs.next()) {
                        final UUID uuid = UUID.fromString(rs.getString(1));
                        createUser(uuid, Bukkit.getOfflinePlayer(uuid).getName());
                        final Location location = LocationUtils.deserializeLocation(rs.getString(3));
                        final Integer userId = base.getUserId(uuid);
                        if (userId == null) {
                            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Invalid UUID " + uuid + ", skipping!"));
                            continue;
                        }
                        final Integer reasonId = base.getReasonId(rs.getString(2));
                        if (reasonId == null) {
                            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Invalid reason " + rs.getString(1) + ", skipping!"));
                            continue;
                        }
                        stmt2.setInt(1, userId);
                        stmt2.setInt(2, reasonId);
                        stmt2.setString(3, location.getWorld().getName());
                        stmt2.setInt(4, location.getBlockX());
                        stmt2.setInt(5, location.getBlockY());
                        stmt2.setInt(6, location.getBlockZ());
                        byte[] data = SerializationUtils.invToBits(getOldSaveById(rs.getInt(4))).readAllBytes();
                        if (data.length > 65535) {
                            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Data size " + data.length + " too large, skipping!"));
                            continue;
                        }
                        stmt2.setBytes(7, data);
                        stmt2.setLong(8, rs.getLong(5));
                        if (rs.getString(6) == null) stmt2.setString(9, null);
                        else stmt2.setString(9, rs.getString(6).equals("---") ? null : rs.getString(6));
                        if (processed % 100 == 0) {
                            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converted: " + processed));
                            stmt2.executeBatch();
                        }
                        processed++;
                        stmt2.addBatch();
                    }
                    stmt2.executeBatch();
                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converted backups!"));
                }
            }

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public ItemStack[] getOldSaveById(int id) {
        final String sql = "SELECT * FROM axinventoryrestore_backups WHERE id = ?;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return SerializationUtils.invFromBase64(rs.getString(2));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void dropOldTables() {
        String sql = "DROP TABLE axinventoryrestore_backups;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();

        } catch (SQLException ignored) {}

        sql = "DROP TABLE axinventoryrestore_uuids;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();

        } catch (SQLException ignored) {}

        sql = "DROP TABLE axinventoryrestore_data;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();

        } catch (SQLException ignored) {}

        if (base instanceof PostgreSQL) {
            sql = "VACUUM;";
        } else if (base instanceof H2) {
            sql = "SHUTDOWN DEFRAG;";
        }

        if (base instanceof MySQL) return;

        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();

        } catch (SQLException ignored) {}
    }

    public void createUser(UUID uuid, @Nullable String name) {
        final String sql = "INSERT INTO axir_users(uuid, name) VALUES (?,?);";
        final String n = name == null ? uuid.toString() : name;
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, n);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            final String sql2 = "UPDATE axir_users SET name = ? WHERE uuid = ?;";
            try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setString(1, n);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();

            } catch (SQLException ex2) {
                ex2.printStackTrace();
            }
        }
    }
}
