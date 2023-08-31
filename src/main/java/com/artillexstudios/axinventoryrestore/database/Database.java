package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axinventoryrestore.utils.BackupData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public interface Database {

    String getType();
    void setup();
    void saveInventory(@NotNull Player player, @NotNull String reason, @Nullable String cause);
    ArrayList<BackupData> getDeathsByType(@NotNull UUID uuid, @NotNull String reason);
    int getDeathsSizeType(@NotNull UUID uuid, @NotNull String reason);
    ArrayList<String> getDeathReasons(@NotNull UUID uuid);
    void join(@NotNull Player player);
    @Nullable UUID getUUID(@NotNull String player);
    void cleanup();
    void disable();
}
