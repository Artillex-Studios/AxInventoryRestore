package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class WorldChange implements Listener {

    @EventHandler
    public void onQuit(@NotNull PlayerChangedWorldEvent event) {
        final String cause = event.getFrom().getName() + " -> " + event.getPlayer().getWorld().getName();
        AxInventoryRestore.getDB().saveInventory(event.getPlayer(), SaveReason.WORLD_CHANGE, cause);
    }
}
