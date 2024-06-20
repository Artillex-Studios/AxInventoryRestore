package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class WorldChangeListener implements Listener {

    @EventHandler
    public void onQuit(@NotNull PlayerChangedWorldEvent event) {
        if (!CONFIG.getBoolean("enabled-backups.world-change", true)) return;
        final String cause = event.getFrom().getName() + " -> " + event.getPlayer().getWorld().getName();
        AxInventoryRestore.getDB().saveInventory(event.getPlayer(), "WORLD_CHANGE", cause);
    }
}
