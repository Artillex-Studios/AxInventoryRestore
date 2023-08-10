package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        final String cause = "---";
        AxInventoryRestore.getDatabaseQueue().submit(() -> {
            AxInventoryRestore.getDB().saveInventory(event.getPlayer(), "QUIT", cause);
        });
    }
}
