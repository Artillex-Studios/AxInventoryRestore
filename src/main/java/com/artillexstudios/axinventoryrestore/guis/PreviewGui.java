package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.api.events.InventoryRestoreEvent;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.discord.DiscordAddon;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.MessageUtils;
import com.artillexstudios.axinventoryrestore.utils.PermissionUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class PreviewGui {
    private final Gui previewGui;
    private final CategoryGui categoryGui;
    private final Player viewer;
    private final UUID restoreUser;
    private final BackupData backupData;
    private final PaginatedGui lastGui;
    private final int pageNum;

    public PreviewGui(@NotNull CategoryGui categoryGui, BackupData backupData, PaginatedGui lastGui, int pageNum) {
        this.categoryGui = categoryGui;
        this.viewer = categoryGui.getViewer();
        this.restoreUser = categoryGui.getRestoreUser();
        this.backupData = backupData;
        this.lastGui = lastGui;
        this.pageNum = pageNum;

        previewGui = Gui.gui()
                .title(ColorUtils.formatToComponent(MESSAGES.getString("guis.previewgui.title").replace("%player%", categoryGui.getMainGui().getName())))
                .rows(6)
                .create();
    }

    public void openPreviewGui() {

        int n = -1;
        for (ItemStack it : backupData.getItems()) {
            n++;
            if (it == null) continue;

            previewGui.setItem(n, ItemBuilder.from(it.clone()).asGuiItem(event -> {
                if (!PermissionUtils.hasPermission(viewer, "modify")) {
                    event.setCancelled(true);
                    return;
                }

                event.setCurrentItem(it);
            }));
        }

        int starter = 46;
        final DiscordAddon discordAddon = AxInventoryRestore.getDiscordAddon();
        if (discordAddon != null) starter = 45;

        previewGui.setItem(starter, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "gui-items.back", Map.of()).getItem()).asGuiItem(event -> {
            lastGui.open(viewer, pageNum);
            event.setCancelled(true);
        }));

        previewGui.setItem(starter + 2, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "guis.previewgui.teleport", Map.of("%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()))).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            if (!PermissionUtils.hasPermission(viewer, "teleport")) {
                MessageUtils.sendMsgP(viewer, "errors.no-permission");
                return;
            }

            PaperUtils.teleportAsync(viewer, backupData.getLocation());
        }));

        previewGui.setItem(starter + 4, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "guis.previewgui.quick-restore", Map.of()).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            if (!PermissionUtils.hasPermission(viewer, "restore")) {
                MessageUtils.sendMsgP(viewer, "errors.no-permission");
                return;
            }

            final Player player = Bukkit.getPlayer(restoreUser);
            if (player == null) {
                MessageUtils.sendMsgP(viewer, "errors.player-offline");
                return;
            }

            final InventoryRestoreEvent inventoryRestoreEvent = new InventoryRestoreEvent(player, backupData);
            Bukkit.getPluginManager().callEvent(inventoryRestoreEvent);
            if (inventoryRestoreEvent.isCancelled()) return;

            int n2 = 0;
            for (ItemStack it : backupData.getItems()) {
                if (it == null) it = new ItemStack(Material.AIR);

                player.getInventory().setItem(n2, it);
                n2++;
            }
        }));

        previewGui.setItem(starter + 6, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "guis.previewgui.export-as-shulker", Map.of("%shulker-amount%", Integer.toString(backupData.getInShulkers(viewer.getName()).size()))).getItem()).asGuiItem(event -> {
            event.setCancelled(true);

            if (!PermissionUtils.hasPermission(viewer, "export")) {
                MessageUtils.sendMsgP(viewer, "errors.no-permission");
                return;
            }

            for (ItemStack it : backupData.getInShulkers(viewer.getName())) {
                viewer.getInventory().addItem(it);
            }
        }));

        if (discordAddon != null) {
            previewGui.setItem(starter + 8, ItemBuilder.from(discordAddon.getRequestItem()).asGuiItem(event -> {
                event.setCancelled(true);

                if (!PermissionUtils.hasPermission(viewer, "discord-request")) {
                    MessageUtils.sendMsgP(viewer, "errors.no-permission");
                    return;
                }

                discordAddon.sendRequest((Player) event.getWhoClicked(), backupData);
            }));
        }

        previewGui.open(viewer);
    }
}
