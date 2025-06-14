package com.artillexstudios.axinventoryrestore.backups;

import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.hooks.AxShulkersHook;
import com.artillexstudios.axinventoryrestore.hooks.HookManager;
import com.artillexstudios.axinventoryrestore.queue.Priority;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import org.bukkit.Location;
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
import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class BackupData {
    private final int id;
    private final UUID player;
    private final String reason;
    private final Location location;
    private final long date;
    private final String cause;
    private final int inventoryId;
    private volatile ItemStack[] items = null;

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

    public CompletableFuture<ItemStack[]> getItems() {
        if (this.items != null) {
            return CompletableFuture.completedFuture(this.items);
        }

        CompletableFuture<ItemStack[]> future = new CompletableFuture<>();
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            ItemStack[] items = AxInventoryRestore.getDB().getItemsFromBackup(inventoryId);
            AxShulkersHook hook = HookManager.getAxShulkersHook();
            if (hook == null) {
                future.complete(items);
                return;
            }

            List<CompletableFuture<ItemStack>> futures = new ArrayList<>();
            for (ItemStack item : items) {
                if (item == null || item.getType().isAir()) continue;

                CompletableFuture<ItemStack> itemFuture = new CompletableFuture<>();
                Scheduler.get().run(task -> {
                    hook.clean(item);
                    itemFuture.complete(item);
                });
                futures.add(itemFuture);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                this.items = items;
                future.complete(items);
            });
        }, Priority.HIGH);
        return future;
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

    public CompletableFuture<ArrayList<ItemStack>> getInShulkers(@NotNull String restorerName) {
        return getItems().thenApply(items -> {
            final ArrayList<ItemStack> shulkerItems = new ArrayList<>();
            final List<ItemStack> itemsCopy = new ArrayList<>();
            itemsCopy.addAll(Arrays.asList(items));

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
        });
    }
}
