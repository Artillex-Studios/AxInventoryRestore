package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class GameModeChangeListener implements Listener {

    @EventHandler
    public void onQuit(@NotNull PlayerGameModeChangeEvent event) {
        if (!CONFIG.getBoolean("enabled-backups.world-change", true)) return;
        final String cause = event.getPlayer().getGameMode().name() + " -> " + event.getNewGameMode().name();
        AxInventoryRestore.getDatabase().saveInventory(event.getPlayer(), "GAMEMODE_CHANGE", cause);
        BackupLimiter.tryLimit(event.getPlayer().getUniqueId(), "gamemode-change", "GAMEMODE_CHANGE");
    }
}
