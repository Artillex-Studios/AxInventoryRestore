package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

public class WorldChange implements Listener {

    @EventHandler
    public void onQuit(@NotNull PlayerChangedWorldEvent event) {
        final String cause = event.getFrom().getName() + " -> " + event.getPlayer().getWorld().getName();
        AxInventoryRestore.getDatabaseQueue().submit(() -> {
            AxInventoryRestore.getDB().saveInventory(event.getPlayer(), "WORLD_CHANGE", cause);
        });
    }
}
