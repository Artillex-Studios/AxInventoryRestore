package com.artillexstudios.axinventoryrestore.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GiveUtils {

    public static void giveItem(@NotNull Player p, ItemStack it, Location location) {

        if (p.getInventory().firstEmpty() == -1) {
            location.getWorld().dropItem(location, it);
            return;
        }

        p.getInventory().addItem(it);
    }
}
