package com.artillexstudios.axinventoryrestore.hooks;

import com.artillexstudios.axshulkers.utils.ShulkerUtils;
import org.bukkit.inventory.ItemStack;

public class AxShulkersHook {

    public void clean(ItemStack item) {
        ShulkerUtils.removeShulkerUUID(item);
    }

}
