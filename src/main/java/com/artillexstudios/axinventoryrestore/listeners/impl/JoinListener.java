package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class JoinListener implements Listener {

    public JoinListener() {
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                load(player);
            }
        });
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            load(event.getPlayer());
            future.complete(null);
        });

        if (!CONFIG.getBoolean("enabled-backups.join", true)) return;
        future.thenRun(() -> {
            Scheduler.get().run(() -> {
                AxInventoryRestore.getDatabase().saveInventory(event.getPlayer(), "JOIN", null);
                BackupLimiter.tryLimit(event.getPlayer().getUniqueId(), "join", "JOIN");
            });
        });
    }

    public void load(Player player) {
        AxInventoryRestore.getDatabase().join(player);
        AxInventoryRestore.getDatabase().fetchRestoreRequests(player.getUniqueId());
    }
}
