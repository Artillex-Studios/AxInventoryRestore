package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BackupData {
    final OfflinePlayer player;
    final SaveReason reason;
    final Location location;
    final ItemStack[] items;
    final long date;

    public BackupData(@NotNull OfflinePlayer player, @NotNull SaveReason reason, @NotNull Location location, @NotNull ItemStack[] items, long date) {
        this.player = player;
        this.reason = reason;
        this.location = location;
        this.items = items;
        this.date = date;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public long getDate() {
        return date;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public SaveReason getReason() {
        return reason;
    }
}
