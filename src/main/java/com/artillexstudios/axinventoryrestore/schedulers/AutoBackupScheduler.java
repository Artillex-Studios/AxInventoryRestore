package com.artillexstudios.axinventoryrestore.schedulers;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutoBackupScheduler {
    private static ScheduledExecutorService executor = null;
    private static ScheduledFuture<?> future = null;

    public static void start() {
        if (future != null) {
            future.cancel(true);
        }

        if (!AxInventoryRestore.CONFIG.getBoolean("automatic-backup.enabled")) {
            return;
        }

        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        final int backupMinutes = AxInventoryRestore.CONFIG.getInt("automatic-backup.minutes", 5);
        final int backupSeconds = AxInventoryRestore.CONFIG.getInt("automatic-backup.seconds", 5);
        final int backupTime = Math.max(1, Math.max(backupMinutes * 60, backupSeconds));

        future = executor.scheduleAtFixedRate(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                AxInventoryRestore.getDB().saveInventory(player, "AUTOMATIC", null);
                BackupLimiter.tryLimit(player.getUniqueId(), "automatic", "AUTOMATIC");
            }
        }, backupTime, backupTime, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (future != null) {
            future.cancel(true);
        }

        if (executor != null) {
            executor.shutdown();
        }
    }
}
