package com.artillexstudios.axinventoryrestore.api;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class AxInventoryRestoreAPI {

    public static void saveInventory(@NotNull Player player, @NotNull String reason, @Nullable String cause) {
        AxInventoryRestore.getDatabase().saveInventory(player, reason.toUpperCase(Locale.ENGLISH), cause);
    }
}
