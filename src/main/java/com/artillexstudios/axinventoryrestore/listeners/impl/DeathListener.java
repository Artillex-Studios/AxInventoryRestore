package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {
        if (!CONFIG.getBoolean("enabled-backups.death", true)) return;
        final Player player = event.getEntity();

        String cause = player.getLastDamageCause() == null ? null : player.getLastDamageCause().getCause().toString();

        if (player.getKiller() != null) {
            cause = cause + " (" + player.getKiller().getName() + ")";
        }

        AxInventoryRestore.getDatabase().saveInventory(player, "DEATH", cause);
        BackupLimiter.tryLimit(player.getUniqueId(), "death", "DEATH");
    }
}
