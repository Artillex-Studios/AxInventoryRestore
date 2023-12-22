package com.artillexstudios.axinventoryrestore.schedulers;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AutoBackupScheduler {

    public void start() {
        if (!AxInventoryRestore.CONFIG.getBoolean("automatic-backup.enabled")) return;
        if (AxInventoryRestore.CONFIG.getLong("automatic-backup.minutes") < 1) return;

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                AxInventoryRestore.getThreadedQueue().submit(() -> {
                    AxInventoryRestore.getDB().saveInventory(player, "AUTOMATIC", null);
                });
            }
        }, 0, AxInventoryRestore.CONFIG.getLong("automatic-backup.minutes"), TimeUnit.MINUTES);
    }
}
