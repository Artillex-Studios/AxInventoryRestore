package com.artillexstudios.axinventoryrestore.schedulers;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AutoBackupScheduler {

    public void start() {
        if (!AxInventoryRestore.CONFIG.getBoolean("automatic-backup.enabled")) return;
        if (AxInventoryRestore.CONFIG.getLong("automatic-backup.minutes") < 1) return;

        Bukkit.getScheduler().runTaskTimer(AxInventoryRestore.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                AxInventoryRestore.getDatabase().saveInventory(player, SaveReason.AUTOMATIC, "---");
            }
        }, 0L, AxInventoryRestore.CONFIG.getLong("automatic-backup.minutes") * 1200L);
    }
}
