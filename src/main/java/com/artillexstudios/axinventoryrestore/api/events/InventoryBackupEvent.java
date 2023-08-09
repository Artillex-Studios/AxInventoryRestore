package com.artillexstudios.axinventoryrestore.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class InventoryBackupEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private final String category;
    private final String extraInfo;
    private boolean isCancelled = false;

    public InventoryBackupEvent(@NotNull Player player, @NotNull String category, @NotNull String extraInfo) {
        this.player = player;
        this.category = category;
        this.extraInfo = extraInfo;
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
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public String getCategory() {
        return category;
    }

    @NotNull
    public String getExtraInfo() {
        return extraInfo;
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
