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

        final int backupMinutes = AxInventoryRestore.CONFIG.getInt("automatic-backup.minutes", 5);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                AxInventoryRestore.getDB().saveInventory(player, "AUTOMATIC", null);
            }
        }, backupMinutes, backupMinutes, TimeUnit.MINUTES);
    }
}
