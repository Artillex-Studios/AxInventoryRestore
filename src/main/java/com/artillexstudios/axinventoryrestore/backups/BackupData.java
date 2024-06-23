package com.artillexstudios.axinventoryrestore.backups;

import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
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
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class BackupData {
    private final int id;
    private final UUID player;
    private final String reason;
    private final Location location;
    private ItemStack[] items = null;
    private final long date;
    private final String cause;
    private final ArrayList<ItemStack> shulkerItems = new ArrayList<>();
    private final int inventoryId;

    public BackupData(int id, @NotNull UUID player, @NotNull String reason, @NotNull Location location, long date, String cause, int inventoryId) {
        this.id = id;
        this.player = player;
        this.reason = reason;
        this.location = location;
        this.date = date;
        this.cause = cause;
        this.inventoryId = inventoryId;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    // todo: make the db request async
    public ItemStack[] getItems() {
        if (items == null) {
            items = AxInventoryRestore.getDB().getItemsFromBackup(inventoryId);
            for (ItemStack it : this.items) {
                if (it == null) continue;
                // axshulkers compatibility
                if (ClassUtils.INSTANCE.classExists("com.artillexstudios.axshulkers.utils.ShulkerUtils")) {
                    if (it.getType().equals(Material.AIR)) continue;
                    com.artillexstudios.axshulkers.utils.ShulkerUtils.removeShulkerUUID(it);
                }
            }
        }
        return items;
    }

    public long getDate() {
        return date;
    }

    public UUID getPlayerUUID() {
        return player;
    }

    public String getReason() {
        return reason;
    }

    public String getCause() {
        return cause;
    }

    public ArrayList<ItemStack> getInShulkers(@NotNull String restorerName) {
        shulkerItems.clear();

        final List<ItemStack> itemsCopy = new ArrayList<>();
        itemsCopy.addAll(Arrays.asList(getItems()));

        while (!itemsCopy.isEmpty()) {

            final Map<String, String> replacements = new HashMap<>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date resultdate = new Date(date);
            replacements.put("%date%", sdf.format(resultdate));
            replacements.put("%location%", LocationUtils.serializeLocationReadable(location));
            replacements.put("%cause%", cause == null ? "---" : cause);
            replacements.put("%staff%", restorerName);
            replacements.put("%player-uuid%", player.toString());

            final ItemStack shulkerIt = new ItemBuilder(MESSAGES.getSection("restored-shulker"), replacements).get();
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

                if (it.getType().toString().endsWith("SHULKER_BOX"))
                    shulkerItems.add(it);
                else
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
