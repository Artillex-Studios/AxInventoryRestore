package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.api.events.InventoryRestoreEvent;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.MessageUtils;
import com.artillexstudios.axinventoryrestore.utils.PermissionUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PreviewGui {
    private final Gui previewGui;
    private final CategoryGui categoryGui;
    private final Player viewer;
    private final OfflinePlayer restoreUser;
    private final BackupData backupData;

    public PreviewGui(@NotNull CategoryGui categoryGui, BackupData backupData) {
        this.categoryGui = categoryGui;
        this.viewer = categoryGui.getViewer();
        this.restoreUser = categoryGui.getRestoreUser();
        this.backupData = backupData;

        previewGui = Gui.gui()
                .title(ColorUtils.formatToComponent(AxInventoryRestore.MESSAGES.getString("guis.previewgui.title").replace("%player%", restoreUser.getName() == null ? "" + restoreUser.getUniqueId() : restoreUser.getName())))
                .rows(6)
                .create();
    }

    public void openPreviewGui() {

        int n = 0;
        for (ItemStack it : backupData.getItems()) {
            if (it == null) it = new ItemStack(Material.AIR);

            previewGui.setItem(n, ItemBuilder.from(it).asGuiItem(event -> {
                if (PermissionUtils.hasPermission(viewer, "modify")) return;
                event.setCancelled(true);
            }));
            n++;
        }

        previewGui.setItem(6, 2, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "gui-items.back", Map.of()).getItem()).asGuiItem(event -> {
            categoryGui.getCategoryGui().open(viewer);
            event.setCancelled(true);
        }));

        previewGui.setItem(6, 4, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "guis.previewgui.teleport", Map.of("%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()))).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            if (!PermissionUtils.hasPermission(viewer, "teleport")) {
                MessageUtils.sendMsgP(viewer, "errors.no-permission");
                return;
            }

            PaperLib.teleportAsync(viewer, backupData.getLocation());
        }));

        previewGui.setItem(6, 6, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "guis.previewgui.quick-restore", Map.of()).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            if (!PermissionUtils.hasPermission(viewer, "restore")) {
                MessageUtils.sendMsgP(viewer, "errors.no-permission");
                return;
            }

            if (restoreUser.getPlayer() == null) {
                MessageUtils.sendMsgP(viewer, "errors.player-offline");
                return;
            }

            final InventoryRestoreEvent inventoryRestoreEvent = new InventoryRestoreEvent(restoreUser.getPlayer(), backupData);
            Bukkit.getPluginManager().callEvent(inventoryRestoreEvent);
            if (inventoryRestoreEvent.isCancelled()) return;

            int n2 = 0;
            for (ItemStack it : backupData.getItems()) {
                if (it == null) it = new ItemStack(Material.AIR);

                restoreUser.getPlayer().getInventory().setItem(n2, it);
                n2++;
            }
        }));

        previewGui.setItem(6, 8, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "guis.previewgui.export-as-shulker", Map.of("%shulker-amount%", Integer.toString(backupData.getInShulkers(viewer).size()))).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            if (!PermissionUtils.hasPermission(viewer, "export")) {
                MessageUtils.sendMsgP(viewer, "errors.no-permission");
                return;
            }

            for (ItemStack it : backupData.getInShulkers(viewer)) {
                viewer.getInventory().addItem(it);
            }
        }));

        previewGui.open(viewer);
    }
}
