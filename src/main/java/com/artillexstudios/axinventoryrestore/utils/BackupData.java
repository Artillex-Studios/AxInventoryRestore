package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BackupData {
    final OfflinePlayer player;
    final String reason;
    final Location location;
    final ItemStack[] items;
    final long date;
    final String cause;
    final ArrayList<ItemStack> shulkerItems = new ArrayList<>();

    public BackupData(@NotNull OfflinePlayer player, @NotNull String reason, @NotNull Location location, @NotNull ItemStack[] items, long date, String cause) {
        this.player = player;
        this.reason = reason;
        this.location = location;
        this.items = items;
        this.date = date;
        this.cause = cause;
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

    public String getReason() {
        return reason;
    }

    public String getCause() {
        return cause;
    }

    public ArrayList<ItemStack> getInShulkers(@NotNull Player restorer) {
        shulkerItems.clear();

        final List<ItemStack> itemsCopy = new ArrayList<>();
        itemsCopy.addAll(Arrays.asList(items));

        while (!itemsCopy.isEmpty()) {

            final Map<String, String> replacements = new HashMap<>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date resultdate = new Date(date);
            replacements.put("%date%", sdf.format(resultdate));
            replacements.put("%location%", LocationUtils.serializeLocationReadable(location));
            replacements.put("%cause%", cause);
            replacements.put("%staff%", restorer.getName());
            replacements.put("%player-uuid%", player.getUniqueId().toString());

            final ItemStack shulkerIt = new ItemBuilder(AxInventoryRestore.MESSAGES, "restored-shulker", replacements).getItem();
            final BlockStateMeta im = (BlockStateMeta) shulkerIt.getItemMeta();
            final ShulkerBox shulker = (ShulkerBox) im.getBlockState();

            final Iterator<ItemStack> iterator = itemsCopy.iterator();
            while (iterator.hasNext()) {
                final ItemStack it = iterator.next();
                if (it == null) {
                    iterator.remove();
                    continue;
                }
                if (shulker.getInventory().firstEmpty() == -1) break;

                shulker.getInventory().addItem(it);
                iterator.remove();
            }

            im.setBlockState(shulker);
            shulkerIt.setItemMeta(im);
            shulkerItems.add(shulkerIt);
        }

        return shulkerItems;
    }
}
