package com.artillexstudios.axinventoryrestore.events;

import com.artillexstudios.axinventoryrestore.api.events.InventoryBackupEvent;
import com.artillexstudios.axinventoryrestore.api.events.InventoryRestoreEvent;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.utils.DateUtils;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class AxirEvents {

    public static boolean callInventoryBackupEvent(@NotNull Player player, @NotNull String category, @Nullable String extraInfo) {
        final InventoryBackupEvent inventoryBackupEvent = new InventoryBackupEvent(player, category, extraInfo);
        Bukkit.getPluginManager().callEvent(inventoryBackupEvent);
        WebHooks.sendBackupWebHook(
                Map.of("%player%", player.getName(),
                    "%category%", MESSAGES.getString("categories." + category + ".raw", category),
                    "%extrainfo%", extraInfo == null ? "---" : extraInfo
                )
        );
        return inventoryBackupEvent.isCancelled();
    }

    public static boolean callInventoryRestoreEvent(@NotNull Player restorer, @NotNull BackupData backupData) {
        final InventoryRestoreEvent inventoryRestoreEvent = new InventoryRestoreEvent(restorer, backupData);
        Bukkit.getPluginManager().callEvent(inventoryRestoreEvent);
        WebHooks.sendRestoreWebHook(
                Map.of("%restorer%", restorer.getName(),
                        "%player%", Bukkit.getOfflinePlayer(backupData.getPlayerUUID()).getName(),
                        "%category%", MESSAGES.getString("categories." + backupData.getReason() + ".raw", backupData.getReason()),
                        "%extrainfo%", backupData.getCause() == null ? "---" : backupData.getCause(),
                        "%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()),
                        "%date%", DateUtils.formatDate(backupData.getDate())
                )
        );
        return inventoryRestoreEvent.isCancelled();
    }

    public static void callBackupExportEvent(@NotNull Player restorer, @NotNull BackupData backupData) {
        WebHooks.sendExportWebHook(
                        Map.of("%restorer%", restorer.getName(),
                        "%category%", MESSAGES.getString("categories." + backupData.getReason() + ".raw", backupData.getReason()),
                        "%player%", Bukkit.getOfflinePlayer(backupData.getPlayerUUID()).getName(),
                        "%extrainfo%", backupData.getCause() == null ? "---" : backupData.getCause(),
                        "%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()),
                        "%date%", DateUtils.formatDate(backupData.getDate())
                )
        );
    }
}
