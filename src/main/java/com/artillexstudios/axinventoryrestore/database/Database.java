package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public interface Database {

    String getType();
    void setup();
    void saveInventory(@NotNull Player player, @NotNull SaveReason reason);
    ArrayList<BackupData> getDeathsByType(@NotNull OfflinePlayer player, @NotNull SaveReason reason);
}
