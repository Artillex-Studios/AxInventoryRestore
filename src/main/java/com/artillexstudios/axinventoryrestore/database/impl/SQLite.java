package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ClassUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.database.Converter2;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.ContainerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class SQLite extends Base {
    private final String url = String.format("jdbc:sqlite:%s/data.db", AxInventoryRestore.getInstance().getDataFolder());

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getType() {
        return "SQLite";
    }

    @Override
    public void setup() {
        ClassUtils.classExists("org.sqlite.JDBC");

        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS axir_backups (\n" +
                "\tid INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\tuserId INT(128) NOT NULL,\n" +
                "\treasonId INT(128) NOT NULL,\n" +
                "\tworld VARCHAR(128) NOT NULL,\n" +
                "\tx INT(128) NOT NULL,\n" +
                "\ty INT(128) NOT NULL,\n" +
                "\tz INT(128) NOT NULL,\n" +
                "\tinventory MEDIUMBLOB NOT NULL,\n" +
                "\ttime BIGINT(128) NOT NULL,\n" +
                "\tcause VARCHAR(128)\n" +
                ");";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String CREATE_TABLE2 = "CREATE TABLE IF NOT EXISTS axir_reasons (\n" +
                "\tid INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\treason VARCHAR(128) NOT NULL,\n" +
                "\tUNIQUE (reason)\n" +
                ");";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE2)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String CREATE_TABLE3 = "CREATE TABLE IF NOT EXISTS axir_users (\n" +
                "\tid INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\tuuid VARCHAR(36) NOT NULL,\n" +
                "\tname VARCHAR(64) NOT NULL,\n" +
                "\tUNIQUE (uuid)\n" +
                ");";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE3)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final String CREATE_TABLE4 = "CREATE TABLE IF NOT EXISTS axir_restorerequests (\n" +
                "\tid INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\tbackupId INT(128) NOT NULL,\n" +
                "\tgranted BOOLEAN DEFAULT 'false'\n" +
                ");";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE4)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            final DatabaseMetaData dmb = getConnection().getMetaData();
            final ResultSet tables = dmb.getTables(null, null, "AXINVENTORYRESTORE_DATA", null);
            if (tables.next()) new Converter2(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override // because sqlite is mid and can't handle multiple queries at once
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
                }
            }

            final String ex = "DELETE FROM axir_restorerequests WHERE granted;";
            try (PreparedStatement stmt2 = conn.prepareStatement(ex)) {
                stmt2.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
