package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.backups.Backup;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.database.Converter2;
import com.artillexstudios.axinventoryrestore.database.Converter3;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.events.AxirEvents;
import com.artillexstudios.axinventoryrestore.utils.SQLUtils;
import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.DISCORD;

public abstract class Base implements Database {
    private static final Logger log = LoggerFactory.getLogger(Base.class);
    private static volatile boolean shutdown = false;
    private final HashBiMap<Integer, UUID> uuidCache = HashBiMap.create();
    private final HashBiMap<Integer, String> reasonCache = HashBiMap.create();
    private final HashBiMap<Integer, String> worldCache = HashBiMap.create();
    private long lastClear = 0;

    public abstract Connection getConnection();

    @Override
    public abstract String getType();

    @Override
    public void setup() {
        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS axir_backups (id INT(128) NOT NULL AUTO_INCREMENT, userId INT(128) NOT NULL, reasonId INT(128) NOT NULL, worldId INT NOT NULL, x INT(128) NOT NULL, y INT(128) NOT NULL, z INT(128) NOT NULL, inventoryId INT NOT NULL, time BIGINT(128) NOT NULL, cause VARCHAR(1024), PRIMARY KEY (id));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while creating axir_backups table!", exception);
        }

        final String CREATE_TABLE2 = "CREATE TABLE IF NOT EXISTS axir_reasons ( id INT(128) NOT NULL AUTO_INCREMENT, reason VARCHAR(1024) NOT NULL, PRIMARY KEY (id), UNIQUE (reason));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE2)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while creating axir_reasons table!", exception);
        }

        final String CREATE_TABLE3 = "CREATE TABLE IF NOT EXISTS axir_users ( id INT(128) NOT NULL AUTO_INCREMENT, uuid VARCHAR(36) NOT NULL, name VARCHAR(512) NOT NULL, PRIMARY KEY (id), UNIQUE (uuid));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE3)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while creating axir_users table!", exception);
        }

        final String CREATE_TABLE4 = "CREATE TABLE IF NOT EXISTS axir_restorerequests ( id INT(128) NOT NULL AUTO_INCREMENT, backupId INT(128) NOT NULL, granted BOOLEAN, PRIMARY KEY (id));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE4)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while creating axir_restorerequests table!", exception);
        }

        final String CREATE_TABLE5 = "CREATE TABLE IF NOT EXISTS axir_storage (id INT NOT NULL AUTO_INCREMENT, inventory MEDIUMBLOB, PRIMARY KEY (id));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE5)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while creating axir_storage table!", exception);
        }

        final String CREATE_TABLE6 = "CREATE TABLE IF NOT EXISTS axir_worlds (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(1024) NOT NULL, PRIMARY KEY (id), UNIQUE (name));";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE6)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while creating axir_worlds table!", exception);
        }

        final String CREATE_INDEX1 = "CREATE INDEX idx_user ON axir_backups (userId);";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_INDEX1)) {
            stmt.executeUpdate();
        } catch (SQLException ignored) {
        }

        try {
            if (SQLUtils.tableExist(getConnection(), "axinventoryrestore_data")) {
                new Converter2(this);
            }
        } catch (Exception exception) {
            log.error("An unexpected error occurred while running v2 converter!", exception);
        }

        try (Statement stmt = getConnection().createStatement()) {
            String query = "SELECT * FROM axir_backups LIMIT 1";
            try (ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData rsmd = rs.getMetaData();
                if (rsmd.getColumnName(8).equalsIgnoreCase("inventory")) {
                    new Converter3(this);
                }
            }
        } catch (Exception exception) {
            log.error("An unexpected error occurred while running v3 converter!", exception);
        }
    }

    @Nullable
    @Override
    public Integer getUserId(@NotNull UUID uuid) {
        Integer userId = uuidCache.inverse().get(uuid);
        if (userId != null) {
            return userId;
        }

        final String sql = "SELECT * FROM axir_users WHERE uuid = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    uuidCache.put(id, uuid);
                    return id;
                }
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting userid for user with uuid {}!", uuid, exception);
        }
        return null;
    }

    @Nullable
    @Override
    public UUID getUserUUID(int id) {
        UUID uid = uuidCache.get(id);
        if (uid != null) {
            return uid;
        }

        final String sql = "SELECT * FROM axir_users WHERE id = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final UUID uuid = UUID.fromString(rs.getString(2));
                    uuidCache.put(id, uuid);
                    return uuid;
                }
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting userid for user with id {}!", id, exception);
        }
        return null;
    }

    @Nullable
    @Override
    public String getReasonName(int id) {
        String cachedReason = reasonCache.get(id);
        if (cachedReason != null) {
            return cachedReason;
        }

        final String sql = "SELECT * FROM axir_reasons WHERE id = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final String reason = rs.getString(2);
                    reasonCache.put(id, reason);
                    return reason;
                }
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting reason from reason-id {}!", id, exception);
        }
        return null;
    }

    @Nullable
    @Override
    public Integer getReasonId(@NotNull String reason) {
        Integer reasonId = reasonCache.inverse().get(reason);
        if (reasonId != null) {
            return reasonId;
        }

        final String sql = "INSERT INTO axir_reasons(reason) VALUES (?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reason);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    reasonCache.put(id, reason);
                    return id;
                }
            }
        } catch (SQLException ex) {
            final String sql2 = "SELECT * FROM axir_reasons WHERE reason = ? LIMIT 1;";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setString(1, reason);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        reasonCache.put(id, reason);
                        return id;
                    }
                }
            } catch (SQLException exception) {
                log.error("An unexpected error occurred while getting reason-id for reason {}!", reason, exception);
            }
        }
        return null;
    }

    @Override
    public void saveInventory(@NotNull Player player, @NotNull String reason, @Nullable String cause) {
        saveInventory(player.getInventory().getContents(), player, reason, cause);
    }

    @Override
    public void saveInventory(ItemStack[] items, @NotNull Player player, @NotNull String reason, @Nullable String cause) {
        if (shutdown) {
            // Don't try to save if the database is already closed
            return;
        }

        if (AxirEvents.callInventoryBackupEvent(player, reason, cause)) {
            return;
        }

        boolean isEmpty = true;

        for (ItemStack it : player.getInventory().getContents()) {
            if (it == null || it.getType().isAir()) continue;
            isEmpty = false;
            break;
        }

        if (isEmpty) {
            return;
        }

        final String sql = "INSERT INTO axir_backups(userId, reasonId, worldId, x, y, z, inventoryId, time, cause) VALUES (?,?,?,?,?,?,?,?,?);";
        final Location location = player.getLocation();

        AxInventoryRestore.getThreadedQueue().submit(() -> {
            byte[] inventory = Serializers.ITEM_ARRAY.serialize(items);

            final Integer userId = getUserId(player.getUniqueId());
            if (userId == null) {
                return;
            }

            // It is most likely that the inventory is the same for the same user in the last backup
            // Sadly this costs more storage space, but it should be a lot faster
            Integer storedId = null;
            Integer backupId = getLastBackupInventoryId(userId);
            if (backupId != null) {
                byte[] bytes = getBytesFromBackup(backupId);
                if (bytes != null) {
                    if (Arrays.equals(inventory, bytes)) {
                        storedId = backupId;
                    }
                }
            }

            if (storedId == null) {
                storedId = storeItems(inventory);
            }

            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, getReasonId(reason));
                stmt.setInt(3, storeWorld(location.getWorld().getName()));
                stmt.setInt(4, location.getBlockX());
                stmt.setInt(5, location.getBlockY());
                stmt.setInt(6, location.getBlockZ());
                stmt.setInt(7, storedId);
                stmt.setLong(8, System.currentTimeMillis());
                stmt.setString(9, cause);
                stmt.executeUpdate();
            } catch (Exception exception) {
                log.error("An unexpected error occurred while saving inventory of user {}!", player.getName(), exception);
            }
        });
    }

    @Override
    public int storeItems(byte[] items) {
        final String sql = "INSERT INTO axir_storage(inventory) VALUES (?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setBytes(1, items);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while storing items!", exception);
        }

        throw new RuntimeException("Failed to save inventory!");
    }

    @Override
    public int storeWorld(String world) {
        Integer worldId = worldCache.inverse().get(world);
        if (worldId != null) {
            return worldId;
        }

        final String sql0 = "SELECT id FROM axir_worlds WHERE name = ? LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql0)) {
            stmt.setString(1, world);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while storing world {}!", world, exception);
        }

        final String sql = "INSERT INTO axir_worlds(name) VALUES (?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, world);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    worldCache.put(id, world);
                    return id;
                }
            }

        } catch (SQLException exception) {
            log.error("An unexpected error occurred while storing world {}!", world, exception);
        }

        throw new RuntimeException("Failed to save world!");

    }

    @Nullable
    @Override
    public World getWorld(int id) {
        String worldName = worldCache.get(id);
        if (worldName != null) {
            return Bukkit.getWorld(worldName);
        }

        final String sql = "SELECT name FROM axir_worlds WHERE id = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString(1);
                    final World world = Bukkit.getWorld(name);
                    worldCache.put(id, name);
                    return world;
                }
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting world with id {}!", id, exception);
        }
        return null;
    }

    @Override
    public Backup getBackupsOfPlayer(@NotNull UUID uuid) {
        final ArrayList<BackupData> backups = new ArrayList<>();

        final String sql = "SELECT id, reasonid, worldId, x, y, z, time, cause, inventoryId FROM axir_backups WHERE userId = ?;";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getUserId(uuid));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final World world = getWorld(rs.getInt(3));
                    if (world == null) continue;
                    backups.add(new BackupData(rs.getInt(1),
                            uuid,
                            getReasonName(rs.getInt(2)),
                            new Location(world, rs.getInt(4), rs.getInt(5), rs.getInt(6)),
                            rs.getLong(7),
                            rs.getString(8),
                            rs.getInt(9))
                    );
                }
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting backups of player {}!", uuid, exception);
        }

        Collections.reverse(backups);
        return new Backup(backups);
    }

    public Integer getLastBackupInventoryId(int userId) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT inventoryId FROM axir_backups WHERE userId = ? ORDER BY id DESC LIMIT 1;")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("inventoryId");
                }
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting last backup id for usre {}!", userId, exception);
        }

        return null;
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

            } catch (SQLException exception) {
                log.error("An unexpected error occurred while updating the name of {}!", player.getName(), exception);
            }
        }
    }

    @Nullable
    @Override
    public UUID getUUID(@NotNull String name) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        if (offlinePlayer.getName() == null) {
            String ex = "SELECT uuid FROM axir_users WHERE name = ? LIMIT 1;";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
                stmt.setString(1, name);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return UUID.fromString(rs.getString(1));
                }
            } catch (SQLException exception) {
                log.error("An unexpected error occurred while getting the uuid of {}!", name, exception);
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

        } catch (SQLException exception) {
            log.error("An unexpected error occurred while adding restore request!", exception);
        }

        return -1;
    }

    @Override
    public void grantRestoreRequest(int restoreId) {
        final String sql = "UPDATE axir_restorerequests SET granted = true WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restoreId);
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while granting restore request with id {}!", restoreId, exception);
        }

        final String sql2 = "SELECT uuid FROM axir_users WHERE id = (SELECT userId FROM axir_backups WHERE id = (SELECT backupId FROM axir_restorerequests WHERE id = ? LIMIT 1) LIMIT 1);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
            stmt.setInt(1, restoreId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) fetchRestoreRequests(UUID.fromString(rs.getString(1)));
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while granting restore request with id {}!", restoreId, exception);
        }
    }

    @Override
    public BackupData getBackupDataById(int backupId) {
        final String sql = "SELECT id, userid, reasonid, worldId, x, y, z, time, cause, inventoryId FROM axir_backups WHERE id = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, backupId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final World world = getWorld(rs.getInt(4));
                    if (world == null) continue;
                    return new BackupData(rs.getInt(1),
                            getUserUUID(rs.getInt(2)),
                            getReasonName(rs.getInt(3)),
                            new Location(world, rs.getInt(5), rs.getInt(6), rs.getInt(7)),
                            rs.getLong(8),
                            rs.getString(9),
                            rs.getInt(10)
                    );
                }
            }

        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting backup data by id {}!", backupId, exception);
        }

        return null;
    }

    public byte[] getBytesFromBackup(int backupId) {
        final String sql = "SELECT inventory FROM axir_storage WHERE id = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, backupId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBytes(1);
            }

        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting items from backup by id {}!", backupId, exception);
        }

        return null;
    }

    @Override
    public ItemStack[] getItemsFromBackup(int backupId) {
        final String sql = "SELECT inventory FROM axir_storage WHERE id = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, backupId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Serializers.ITEM_ARRAY.deserialize(rs.getBytes(1));
            }

        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting items from backup by id {}!", backupId, exception);
        }

        return null;
    }

    @Override
    public int getSaves(UUID uuid, @Nullable String reason) {
        String noReason = "SELECT COUNT(*) FROM axir_backups WHERE userId = ?;";
        String withReason = "SELECT COUNT(*) FROM axir_backups WHERE userId = ? AND reasonId = ?;";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(reason == null ? noReason : withReason)) {
            statement.setInt(1, getUserId(uuid));
            if (reason != null) {
                statement.setInt(2, getReasonId(reason));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }

                return 0;
            }
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while getting save-count for {}!", uuid, exception);
            return -1;
        }
    }

    @Override
    public void removeLastSaves(UUID uuid, @Nullable String reason, int amount) {
        final String noReason = "DELETE FROM axir_backups WHERE id IN (SELECT id FROM axir_backups WHERE userId = ? ORDER BY time ASC LIMIT ?);";
        final String withReason = "DELETE FROM axir_backups WHERE id IN (SELECT id FROM axir_backups WHERE userId = ? AND reasonId = ? ORDER BY time ASC LIMIT ?);";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(reason == null ? noReason : withReason)) {
            if (reason == null) {
                statement.setInt(1, getUserId(uuid));
                statement.setInt(2, amount);
            } else {
                statement.setInt(1, getUserId(uuid));
                statement.setInt(2, getReasonId(reason));
                statement.setInt(3, amount);
            }
            statement.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while removing last save for {}!", uuid, exception);
        }

        clean();
    }

    private void clean() {
        if (System.currentTimeMillis() - lastClear < Duration.ofSeconds(60).toMillis()) {
            return;
        }
        lastClear = System.currentTimeMillis();

        final String sql2 = "DELETE FROM axir_storage WHERE id not IN (SELECT inventoryId FROM axir_backups WHERE inventoryId IS NOT NULL);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while cleaning up!", exception);
        }
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

                    backupData.getInShulkers("---").thenAccept(items -> Scheduler.get().run(scheduledTask -> ContainerUtils.INSTANCE.addOrDrop(player.getInventory(), items, player.getLocation())));
                    player.sendMessage(StringUtils.formatToString(CONFIG.getString("prefix") + DISCORD.getString("messages.restored")));
                    int id = rs.getInt(1);
                    removeRestoreRequest(id);
                }
            }

        } catch (SQLException exception) {
            log.error("An unexpected error occurred while fetching restore request for user with uuid {}!", uuid, exception);
        }
    }

    @Override
    public void removeRestoreRequest(int restoreId) {
        final String ex = "DELETE FROM axir_restorerequests WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(ex)) {
            stmt.setInt(1, restoreId);
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while removing restore request!", exception);
        }
    }

    @Override
    public void cleanup() {
        final String sql = "DELETE FROM axir_backups WHERE time < ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis() - (86_400_000L * AxInventoryRestore.CONFIG.getLong("cleanup-after-days")));
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while cleaning up!", exception);
        }

        final String sql2 = "DELETE FROM axir_storage WHERE id not IN ( SELECT inventoryId FROM axir_backups WHERE inventoryId IS NOT NULL);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while cleaning up!", exception);
        }

        final String sql3 = "DELETE FROM axir_worlds WHERE id not IN ( SELECT worldId FROM axir_backups WHERE worldId IS NOT NULL);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql3)) {
            stmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred while cleaning up!", exception);
        }
    }

    @Override
    public void disable() {
        shutdown = true;
    }
}
