package com.artillexstudios.axinventoryrestore.listeners.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class EnderChestCloseListener implements Listener {

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) return;
        if (!CONFIG.getBoolean("enabled-backups.ender-chest", true)) return;
        AxInventoryRestore.getDB().saveInventory(event.getPlayer().getEnderChest().getStorageContents(), (Player) event.getPlayer(), "ENDER_CHEST", null);
        BackupLimiter.tryLimit(event.getPlayer().getUniqueId(), "ender-chest", "ENDER_CHEST");
    }
}
