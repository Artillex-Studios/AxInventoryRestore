package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        final String cause = "---";
        AxInventoryRestore.getDatabaseQueue().submit(() -> {
            AxInventoryRestore.getDB().saveInventory(event.getPlayer(), "JOIN", cause);
        });
    }
}
