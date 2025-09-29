package com.artillexstudios.axinventoryrestore.schedulers;

import com.artillexstudios.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class AutoBackupScheduler {
    private static ExceptionReportingScheduledThreadPool pool = null;

    public static void start() {
        if (pool != null) pool.shutdown();
        if (!CONFIG.getBoolean("automatic-backup.enabled")) return;

        pool = new ExceptionReportingScheduledThreadPool(1);
        int backupMinutes = CONFIG.getInt("automatic-backup.minutes", 5);
        int backupSeconds = CONFIG.getInt("automatic-backup.seconds", 5);
        int backupTime = Math.max(1, Math.max(backupMinutes * 60, backupSeconds));

        pool.scheduleAtFixedRate(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                AxInventoryRestore.getDatabase().saveInventory(player, "AUTOMATIC", null);
                BackupLimiter.tryLimit(player.getUniqueId(), "automatic", "AUTOMATIC");
            }
        }, backupTime, backupTime, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (pool == null) return;
        pool.shutdown();
    }
}
