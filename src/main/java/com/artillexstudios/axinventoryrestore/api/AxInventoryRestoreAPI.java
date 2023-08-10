package com.artillexstudios.axinventoryrestore.api;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AxInventoryRestoreAPI {

    public static void saveInventory(@NotNull Player player, @NotNull String category, @NotNull String extraInfo) {
        AxInventoryRestore.getDatabaseQueue().submit(() -> {
            AxInventoryRestore.getDB().saveInventory(player, category, extraInfo);
        });
    }
}
