package com.artillexstudios.axinventoryrestore.api.events;

import com.artillexstudios.axinventoryrestore.utils.BackupData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class InventoryRestoreEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Player restorer;
    private final BackupData backupData;
    private boolean isCancelled = false;

    public InventoryRestoreEvent(@NotNull Player restorer, @NotNull BackupData backupData) {
        super(!Bukkit.isPrimaryThread());

        this.restorer = restorer;
        this.backupData = backupData;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    public Player getRestorer() {
        return restorer;
    }

    @NotNull
    public BackupData getBackupData() {
        return backupData;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
}
