package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        if (!CONFIG.getBoolean("enabled-backups.join", true)) return;

        CompletableFuture<Void> future = new CompletableFuture<>();
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            AxInventoryRestore.getDatabase().join(event.getPlayer());
            AxInventoryRestore.getDatabase().fetchRestoreRequests(event.getPlayer().getUniqueId());
            future.complete(null);
        });

        future.thenRun(() -> {
            Scheduler.get().run(() -> {
                AxInventoryRestore.getDatabase().saveInventory(event.getPlayer(), "JOIN", null);
                BackupLimiter.tryLimit(event.getPlayer().getUniqueId(), "join", "JOIN");
            });
        });
    }
}
