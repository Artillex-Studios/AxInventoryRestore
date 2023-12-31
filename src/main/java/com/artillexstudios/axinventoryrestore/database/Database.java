package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axinventoryrestore.backups.Backup;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public interface Database {

    String getType();
    void setup();
    void saveInventory(@NotNull Player player, @NotNull String reason, @Nullable String cause);
    Backup getDeathsOfPlayer(@NotNull UUID uuid);
    void join(@NotNull Player player);
    @Nullable UUID getUUID(@NotNull String player);
    int addRestoreRequest(int backupId);
    void grantRestoreRequest(int restoreId);
    BackupData getBackupDataById(int backupId);
    void fetchRestoreRequests(@NotNull UUID uuid);
    void removeRestoreRequest(int restoreId);
    void cleanup();
    void disable();
}
