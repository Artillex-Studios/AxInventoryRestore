package com.artillexstudios.axinventoryrestore.utils;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class ContainerUtils {

    public static void addOrDrop(Inventory inventory, List<ItemStack> items, Location location) {
        for (ItemStack key : items) {
            final HashMap<Integer, ItemStack> remaining = inventory.addItem(key);
            remaining.forEach((k, v) -> location.getWorld().dropItem(location, v));
        }
    }
}
