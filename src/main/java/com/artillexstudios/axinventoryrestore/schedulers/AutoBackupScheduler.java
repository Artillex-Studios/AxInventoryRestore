package com.artillexstudios.axinventoryrestore.schedulers;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutoBackupScheduler {
    private static ScheduledFuture<?> future = null;

    public static void start() {
        if (future != null) future.cancel(true);

        if (!AxInventoryRestore.CONFIG.getBoolean("automatic-backup.enabled")) return;
        final int backupMinutes = AxInventoryRestore.CONFIG.getInt("automatic-backup.minutes", 5);
        final int backupSeconds = AxInventoryRestore.CONFIG.getInt("automatic-backup.seconds", 5);
        final int backupTime = Math.max(1, Math.max(backupMinutes * 60, backupSeconds));

        future = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                AxInventoryRestore.getDB().saveInventory(player, "AUTOMATIC", null);
            }
        }, backupTime, backupTime, TimeUnit.SECONDS);
    }

    public static void stop() {
        future.cancel(true);
    }
}
