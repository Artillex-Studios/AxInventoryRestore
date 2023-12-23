package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.api.events.InventoryBackupEvent;
import com.artillexstudios.axinventoryrestore.backups.Backup;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.database.Converter2;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.ContainerUtils;
import com.artillexstudios.axinventoryrestore.utils.SQLUtils;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
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

public class Base implements Database {
    public Connection getConnection() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void setup() {
        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS axir_backups (id INT(128) NOT NULL AUTO_INCREMENT, userId INT(128) NOT NULL, reasonId INT(128) NOT NULL, world VARCHAR(128) NOT NULL, x INT(128) NOT NULL, y INT(128) NOT NULL, z INT(128) NOT NULL, inventory MEDIUMBLOB(16777215) NOT NULL, time BIGINT(128) NOT NULL, cause VARCHAR(128), PRIMARY KEY (id));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        final String CREATE_TABLE2 = "CREATE TABLE IF NOT EXISTS axir_reasons ( id INT(128) NOT NULL AUTO_INCREMENT, reason VARCHAR(128) NOT NULL, PRIMARY KEY (id), UNIQUE (reason));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE2)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        final String CREATE_TABLE3 = "CREATE TABLE IF NOT EXISTS axir_users ( id INT(128) NOT NULL AUTO_INCREMENT, uuid VARCHAR(36) NOT NULL, name VARCHAR(64) NOT NULL, PRIMARY KEY (id), UNIQUE (uuid));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE3)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        final String CREATE_TABLE4 = "CREATE TABLE IF NOT EXISTS axir_restorerequests ( id INT(128) NOT NULL AUTO_INCREMENT, backupId INT(128) NOT NULL, granted BOOLEAN, PRIMARY KEY (id));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE4)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            if (SQLUtils.tableExist(getConnection(), "axinventoryrestore_data")) new Converter2(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Integer getUserId(@NotNull UUID uuid) {
        final String sql = "SELECT * FROM axir_users WHERE uuid = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public UUID getUserUUID(int id) {
        final String sql = "SELECT * FROM axir_users WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return UUID.fromString(rs.getString(2));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getReasonName(int id) {
        final String sql = "SELECT * FROM axir_reasons WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString(2);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Integer getReasonId(@NotNull String reason) {
        final String sql = "INSERT INTO axir_reasons(reason) VALUES (?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reason);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            final String sql2 = "SELECT * FROM axir_reasons WHERE reason = ? LIMIT 1;";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setString(1, reason);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException ex2) {
                ex2.printStackTrace();
            }
        }
        return null;
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

        final String sql = "INSERT INTO axir_backups(userId, reasonId, world, x, y, z, inventory, time, cause) VALUES (?,?,?,?,?,?,?,?,?);";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getUserId(player.getUniqueId()));
            stmt.setInt(2, getReasonId(reason));
            stmt.setString(3, player.getLocation().getWorld().getName());
            stmt.setInt(4, player.getLocation().getBlockX());
            stmt.setInt(5, player.getLocation().getBlockY());
            stmt.setInt(6, player.getLocation().getBlockZ());
            stmt.setBytes(7, SerializationUtils.invToBits(player.getInventory().getContents()).readAllBytes());
            stmt.setLong(8, System.currentTimeMillis());
            stmt.setString(9, cause);
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Backup getDeathsOfPlayer(@NotNull UUID uuid) {
        final ArrayList<BackupData> backups = new ArrayList<>();

        final String sql = "SELECT * FROM axir_backups WHERE userId = ? ORDER BY time DESC;";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getUserId(uuid));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final World world = Bukkit.getWorld(rs.getString(4));
                    if (world == null) continue;
                    backups.add(new BackupData(rs.getInt(1),
                            getUserUUID(rs.getInt(2)),
                            getReasonName(rs.getInt(3)),
                            new Location(world, rs.getInt(5), rs.getInt(6), rs.getInt(7)),
                            SerializationUtils.invFromBits(rs.getBinaryStream(8)),
                            rs.getLong(9),
                            rs.getString(10))
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return new Backup(backups);
    }

    @Override
    public void join(@NotNull Player player) {
        final String sql = "INSERT INTO axir_users(uuid, name) VALUES (?,?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.executeUpdate();

        } catch (SQLException ex) {
            final String sql2 = "UPDATE axir_users SET name = ? WHERE uuid = ?;";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setString(1, player.getName());
                stmt.setString(2, player.getUniqueId().toString());
                stmt.executeUpdate();

            } catch (SQLException ex2) {
                ex2.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public UUID getUUID(@NotNull String name) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        if (offlinePlayer == null) {

            String ex = "SELECT uuid FROM axir_users WHERE name = ? LIMIT 1;";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
                stmt.setString(1, name);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return UUID.fromString(rs.getString(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        return offlinePlayer.getUniqueId();
    }

    @Override
    public int addRestoreRequest(int backupId) {
        final String sql = "INSERT INTO axir_restorerequests(backupId, granted) VALUES (?, false);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, backupId);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    @Override
    public void grantRestoreRequest(int restoreId) {
        final String sql = "UPDATE axir_restorerequests SET granted = true WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restoreId);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        final String sql2 = "SELECT uuid FROM axir_users WHERE id = (SELECT userId FROM axir_backups WHERE id = (SELECT backupId FROM axir_restorerequests WHERE id = ? LIMIT 1) LIMIT 1);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
            stmt.setInt(1, restoreId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) fetchRestoreRequests(UUID.fromString(rs.getString(1)));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public BackupData getBackupDataById(int backupId) {
        final String sql = "SELECT * FROM axir_backups WHERE id = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, backupId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final World world = Bukkit.getWorld(rs.getString(4));
                    if (world == null) continue;
                    return new BackupData(rs.getInt(1),
                            getUserUUID(rs.getInt(2)),
                            getReasonName(rs.getInt(3)),
                            new Location(world, rs.getInt(5), rs.getInt(6), rs.getInt(7)),
                            SerializationUtils.invFromBits(rs.getBinaryStream(8)),
                            rs.getLong(9),
                            rs.getString(10)
                    );
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public void fetchRestoreRequests(@NotNull UUID uuid) {
        if (AxInventoryRestore.getDiscordAddon() == null) return;
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        final String sql = "SELECT * FROM axir_restorerequests WHERE granted AND backupId IN (SELECT id FROM axir_backups WHERE userId = (SELECT id FROM axir_users WHERE uuid = ? LIMIT 1));";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final BackupData backupData = getBackupDataById(rs.getInt(2));
                    Scheduler.get().run(scheduledTask -> ContainerUtils.addOrDrop(player.getInventory(), backupData.getInShulkers("---"), player.getLocation()));

                    player.sendMessage(ColorUtils.format(CONFIG.getString("prefix") + AxInventoryRestore.getDiscordAddon().DISCORDCONFIG.getString("messages.restored")));
                    int id = rs.getInt(1);
                    removeRestoreRequest(id);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeRestoreRequest(int restoreId) {
        final String ex = "DELETE FROM axir_restorerequests WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setInt(1, restoreId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        final String ex = "DELETE FROM axir_backups WHERE time < ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setLong(1, System.currentTimeMillis() - (86_400_000L * AxInventoryRestore.CONFIG.getLong("cleanup-after-days")));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disable() {
    }
}
