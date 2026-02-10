package com.artillexstudios.axinventoryrestore.api;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class AxInventoryRestoreAPI {

    public static void saveInventory(@NotNull Player player, @NotNull String reason, @Nullable String cause) {
        AxInventoryRestore.getDatabase().saveInventory(player, reason.toUpperCase(Locale.ENGLISH), cause);
        BackupLimiter.tryLimit(player.getUniqueId(), reason.toLowerCase(Locale.ENGLISH), reason.toUpperCase(Locale.ENGLISH));
    }
}
