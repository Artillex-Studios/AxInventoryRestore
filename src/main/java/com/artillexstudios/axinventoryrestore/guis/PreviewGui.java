package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.MessageUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
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
                .title(ColorUtils.deserialize(AxInventoryRestore.MESSAGES.getString("guis.previewgui.title").replace("%player%", restoreUser.getName() == null ? "" + restoreUser.getUniqueId() : restoreUser.getName())))
                .rows(6)
                .create();
    }

    public void openPreviewGui() {

        int n = 0;
        for (ItemStack it : backupData.getItems()) {
            if (it == null) it = new ItemStack(Material.AIR);

            previewGui.setItem(n, ItemBuilder.from(it).asGuiItem());
            n++;
        }

        previewGui.setItem(6, 2, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "gui-items.back", Map.of()).getItem()).asGuiItem(event -> {
            categoryGui.getCategoryGui().open(viewer);
            event.setCancelled(true);
        }));

        previewGui.setItem(6, 4, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "guis.previewgui.teleport", Map.of("%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()))).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            viewer.teleport(backupData.getLocation());
        }));

        previewGui.setItem(6, 6, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "guis.previewgui.quick-restore", Map.of()).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            if (restoreUser.getPlayer() == null) {
                MessageUtils.sendMsgP(viewer, "errors.player-offline");
                return;
            }

            int n2 = 0;
            for (ItemStack it : backupData.getItems()) {
                if (it == null) it = new ItemStack(Material.AIR);

                restoreUser.getPlayer().getInventory().setItem(n2, it);
                n2++;
            }
        }));

        previewGui.setItem(6, 8, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "guis.previewgui.export-as-shulker", Map.of("%shulker-amount%", Integer.toString(backupData.getInShulkers(viewer).size()))).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            for (ItemStack it : backupData.getInShulkers(viewer)) {
                viewer.getInventory().addItem(it);
            }
        }));

        previewGui.open(viewer);
    }
}
