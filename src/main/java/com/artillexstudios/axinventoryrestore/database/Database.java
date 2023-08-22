package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axinventoryrestore.utils.BackupData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public interface Database {

    String getType();
    void setup();
    void saveInventory(@NotNull Player player, @NotNull String reason, @Nullable String cause);
    ArrayList<BackupData> getDeathsByType(@NotNull OfflinePlayer player, @NotNull String reason);
    int getDeathsSizeType(@NotNull OfflinePlayer player, @NotNull String reason);
    ArrayList<String> getDeathReasons(@NotNull OfflinePlayer player);
    void cleanup();
    void disable();
}
