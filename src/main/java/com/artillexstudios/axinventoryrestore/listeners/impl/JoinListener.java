package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        if (!CONFIG.getBoolean("enabled-backups.join", true)) return;
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            AxInventoryRestore.getDB().join(event.getPlayer());
            AxInventoryRestore.getDB().fetchRestoreRequests(event.getPlayer().getUniqueId());
        });
        AxInventoryRestore.getDB().saveInventory(event.getPlayer(), "JOIN", null);
        BackupLimiter.tryLimit(event.getPlayer().getUniqueId(), "join", "JOIN");
    }
}
