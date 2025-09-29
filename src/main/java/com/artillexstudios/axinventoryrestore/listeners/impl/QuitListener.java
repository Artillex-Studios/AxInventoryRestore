package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        if (!CONFIG.getBoolean("enabled-backups.quit", true)) return;
        AxInventoryRestore.getDatabase().saveInventory(event.getPlayer(), "QUIT", null);
        BackupLimiter.tryLimit(event.getPlayer().getUniqueId(), "quit", "QUIT");
    }
}
