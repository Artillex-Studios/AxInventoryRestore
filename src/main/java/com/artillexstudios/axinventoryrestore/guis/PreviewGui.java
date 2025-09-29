package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.discord.DiscordAddon;
import com.artillexstudios.axinventoryrestore.events.AxirEvents;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import com.artillexstudios.axinventoryrestore.utils.PermissionUtils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

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
                .title(StringUtils.format(MESSAGES.getString("guis.previewgui.title").replace("%player%", categoryGui.getMainGui().getName())))
                .rows(6)
                .create();
    }

    public void open() {
        long time = System.currentTimeMillis();
        if (AxInventoryRestore.isDebugMode()) LogUtils.debug("Opening backup preview for {}", viewer.getName());
        backupData.getItems().thenAccept(items -> {
            if (AxInventoryRestore.isDebugMode()) LogUtils.debug("Preview data loaded for {} in {}ms", viewer.getName(), System.currentTimeMillis() - time);
            int n = -1;

            for (ItemStack it : items) {
                n++;
                if (it == null) continue;
                if (it.getType().isAir()) continue;

                previewGui.setItem(n, new GuiItem(it, event -> {
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

            previewGui.setItem(starter, new GuiItem(ItemBuilder.create(MESSAGES.getSection("gui-items.back")).get(), event -> {
                Scheduler.get().runAt(viewer.getLocation(), task -> lastGui.open(viewer, pageNum));
                event.setCancelled(true);
            }));

            previewGui.setItem(starter + 2, new GuiItem(ItemBuilder.create(MESSAGES.getSection("guis.previewgui.teleport"), Map.of("%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()))).get(), event -> {
                event.setCancelled(true);

                if (!PermissionUtils.hasPermission(viewer, "teleport")) {
                    MESSAGEUTILS.sendLang(viewer, "errors.no-permission");
                    return;
                }

                Scheduler.get().runAt(viewer.getLocation(), () ->
                        PaperUtils.teleportAsync(viewer, backupData.getLocation().clone())
                                .thenRun(() -> Scheduler.get().runAt(viewer.getLocation(), viewer::closeInventory)));
            }));

            boolean isEnder = backupData.getReason().equals("ENDER_CHEST");
            previewGui.setItem(starter + 4, new GuiItem(ItemBuilder.create(MESSAGES.getSection("guis.previewgui.quick-restore" + (isEnder ? "-ender-chest" : ""))).get(), event -> {
                event.setCancelled(true);

                if (!PermissionUtils.hasPermission(viewer, "restore")) {
                    MESSAGEUTILS.sendLang(viewer, "errors.no-permission");
                    return;
                }

                final Player player = Bukkit.getPlayer(restoreUser);
                if (player == null) {
                    MESSAGEUTILS.sendLang(viewer, "errors.player-offline");
                    return;
                }

                if (AxirEvents.callInventoryRestoreEvent(viewer, backupData)) return;

                int n2 = 0;
                for (ItemStack it : items) {
                    if (it == null) it = new ItemStack(Material.AIR);

                    if (isEnder)
                        player.getEnderChest().setItem(n2, it);
                    else
                        player.getInventory().setItem(n2, it);
                    n2++;
                }
            }));

            final int starterFinal = starter;
            backupData.getInShulkers(viewer.getName()).thenAccept(item -> {
                previewGui.setItem(starterFinal + 6, new GuiItem(ItemBuilder.create(MESSAGES.getSection("guis.previewgui.export-as-shulker"), Map.of("%shulker-amount%", Integer.toString(item.size()))).get(), event -> {
                    event.setCancelled(true);

                    if (!PermissionUtils.hasPermission(viewer, "export")) {
                        MESSAGEUTILS.sendLang(viewer, "errors.no-permission");
                        return;
                    }

                    AxirEvents.callBackupExportEvent(viewer, backupData);

                    for (ItemStack i : item) {
                        viewer.getInventory().addItem(i);
                    }
                }));
                previewGui.update();
            });

            if (discordAddon != null) {
                previewGui.setItem(starter + 8, new GuiItem(discordAddon.getRequestItem(), event -> {
                    event.setCancelled(true);

                    if (!PermissionUtils.hasPermission(viewer, "discord-request")) {
                        MESSAGEUTILS.sendLang(viewer, "errors.no-permission");
                        return;
                    }

                    discordAddon.sendRequest((Player) event.getWhoClicked(), backupData);
                }));
            }

            previewGui.update();
        });

        Scheduler.get().runAt(viewer.getLocation(), () -> previewGui.open(viewer));
        if (AxInventoryRestore.isDebugMode()) LogUtils.debug("Preview gui opened for {} in {}ms", viewer.getName(), System.currentTimeMillis() - time);
    }
}
