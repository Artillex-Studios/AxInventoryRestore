package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {
        String cause = event.getPlayer().getLastDamageCause() == null ? "---" : event.getPlayer().getLastDamageCause().getCause().toString();

        if (event.getPlayer().getKiller() != null) {
            cause = cause + " (" + event.getPlayer().getKiller().getName() + ")";
        }

        AxInventoryRestore.getDatabase().saveInventory(event.getPlayer(), SaveReason.DEATH, cause);
    }
}
