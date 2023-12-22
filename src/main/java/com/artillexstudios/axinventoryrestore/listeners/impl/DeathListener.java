package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {
        final Player player = event.getEntity();

        String cause = player.getLastDamageCause() == null ? null : player.getLastDamageCause().getCause().toString();

        if (player.getKiller() != null) {
            cause = cause + " (" + player.getKiller().getName() + ")";
        }

        String finalCause = cause;
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            AxInventoryRestore.getDB().saveInventory(player, "DEATH", finalCause);
        });
    }
}
