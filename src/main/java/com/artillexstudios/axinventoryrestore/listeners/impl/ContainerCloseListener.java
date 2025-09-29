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

public class ContainerCloseListener implements Listener {

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().getLocation() == null) return;
        var type = event.getInventory().getType();
        if (type != InventoryType.CHEST
                && type != InventoryType.BARREL
                && type != InventoryType.SHULKER_BOX
                && type != InventoryType.HOPPER
                && type != InventoryType.DISPENSER
                && type != InventoryType.DROPPER) return;
        if (!CONFIG.getBoolean("enabled-backups.container-close", true)) return;
        final String cause = event.getInventory().getType().name();
        AxInventoryRestore.getDatabase().saveInventory((Player) event.getPlayer(), "CONTAINER_CLOSE", cause);
        BackupLimiter.tryLimit(event.getPlayer().getUniqueId(), "container-close", "CONTAINER_CLOSE");
    }
}
