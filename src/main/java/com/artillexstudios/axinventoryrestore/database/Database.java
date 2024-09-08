package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axinventoryrestore.backups.Backup;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Database {

    String getType();

    void setup();

    @Nullable
    Integer getUserId(@NotNull UUID uuid);

    @Nullable
    UUID getUserUUID(int id);

    @Nullable
    String getReasonName(int id);

    @Nullable
    Integer getReasonId(@NotNull String reason);

    void saveInventory(@NotNull Player player, @NotNull String reason, @Nullable String cause);

    void saveInventory(ItemStack[] items, @NotNull Player player, @NotNull String reason, @Nullable String cause);

    int storeItems(byte[] items);

    int storeWorld(String world);

    @Nullable
    World getWorld(int id);

    Backup getBackupsOfPlayer(@NotNull UUID uuid);

    void join(@NotNull Player player);

    @Nullable
    UUID getUUID(@NotNull String player);

    int addRestoreRequest(int backupId);

    void grantRestoreRequest(int restoreId);

    BackupData getBackupDataById(int backupId);

    ItemStack[] getItemsFromBackup(int backupId);

    // Null reason is any reason
    int getSaves(UUID uuid, @Nullable String reason);

    // Null reason is any reason
    void removeLastSaves(UUID uuid, @Nullable String reason, int amount);

    void fetchRestoreRequests(@NotNull UUID uuid);

    void removeRestoreRequest(int restoreId);

    void cleanup();

    void disable();
}
