package com.artillexstudios.axinventoryrestore.api;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AxInventoryRestoreAPI {

    public static void saveInventory(@NotNull Player player, @NotNull String category, @Nullable String extraInfo) {
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            AxInventoryRestore.getDB().saveInventory(player, category, extraInfo);
        });
    }
}
