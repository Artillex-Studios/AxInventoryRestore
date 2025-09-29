package com.artillexstudios.axinventoryrestore.api;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AxInventoryRestoreAPI {

    public static void saveInventory(@NotNull Player player, @NotNull String category, @Nullable String extraInfo) {
        AxInventoryRestore.getDatabase().saveInventory(player, category, extraInfo);
        BackupLimiter.tryLimit(player.getUniqueId(), category, category);
    }
}
