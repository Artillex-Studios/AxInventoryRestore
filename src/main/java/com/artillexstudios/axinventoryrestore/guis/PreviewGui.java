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
import com.artillexstudios.axinventoryrestore.search.OpenDetails;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.LANG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public class PreviewGui {
    private final OpenDetails details;
    private final Gui previewGui;
    private final Player viewer;
    private final BackupData backupData;
    private final PaginatedGui lastGui;
    private final int pageNum;

    public PreviewGui(OpenDetails details, Player viewer, BackupData backupData, PaginatedGui lastGui, int pageNum) {
        this.details = details;
        this.viewer = viewer;
        this.backupData = backupData;
        this.lastGui = lastGui;
        this.pageNum = pageNum;

        previewGui = Gui.gui()
                .title(StringUtils.format(LANG.getString("guis.previewgui.title").replace("%player%", details.getName())))
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
                    if (!viewer.hasPermission("axinventoryrestore.modify")) {
                        event.setCancelled(true);
                        return;
                    }

                    event.setCurrentItem(it);
                }));
            }

            int starter = 46;
            final DiscordAddon discordAddon = AxInventoryRestore.getDiscordAddon();
            if (discordAddon != null) starter = 45;

            previewGui.setItem(starter, new GuiItem(ItemBuilder.create(LANG.getSection("gui-items.back")).get(), event -> {
                lastGui.open(viewer, pageNum);
                event.setCancelled(true);
            }));

            previewGui.setItem(starter + 2, new GuiItem(ItemBuilder.create(LANG.getSection("guis.previewgui.teleport"), Map.of("%location%", backupData.getLocation().getReadable())).get(), event -> {
                event.setCancelled(true);

                if (!viewer.hasPermission("axinventoryrestore.teleport")) {
                    MESSAGEUTILS.sendLang(viewer, "errors.no-permission");
                    return;
                }

                Location location = backupData.getLocation().get();
                if (location == null) {
                    MESSAGEUTILS.sendLang(viewer, "errors.world-not-found");
                    return;
                }
                Scheduler.get().execute(viewer, () -> {
                    viewer.closeInventory();
                    PaperUtils.teleportAsync(viewer, location);
                }, () -> {}, 0);
            }));

            boolean isEnder = backupData.getReason().equals("ENDER_CHEST");
            previewGui.setItem(starter + 4, new GuiItem(ItemBuilder.create(LANG.getSection("guis.previewgui.quick-restore" + (isEnder ? "-ender-chest" : ""))).get(), event -> {
                event.setCancelled(true);

                if (!viewer.hasPermission("axinventoryrestore.restore")) {
                    MESSAGEUTILS.sendLang(viewer, "errors.no-permission");
                    return;
                }

                Player player = Bukkit.getPlayer(backupData.getPlayerUUID());
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
                previewGui.setItem(starterFinal + 6, new GuiItem(ItemBuilder.create(LANG.getSection("guis.previewgui.export-as-shulker"), Map.of("%shulker-amount%", Integer.toString(item.size()))).get(), event -> {
                    event.setCancelled(true);

                    if (!viewer.hasPermission("axinventoryrestore.export")) {
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

                    if (!viewer.hasPermission("axinventoryrestore.discord-request")) {
                        MESSAGEUTILS.sendLang(viewer, "errors.no-permission");
                        return;
                    }

                    discordAddon.sendRequest((Player) event.getWhoClicked(), backupData).thenAccept(success -> {
                        MESSAGEUTILS.sendLang(viewer, "discord-request." + (success ? "success" : "failure"));
                    });
                }));
            }

            previewGui.update();
        });

        previewGui.open(viewer);
        if (AxInventoryRestore.isDebugMode()) LogUtils.debug("Preview gui opened for {} in {}ms", viewer.getName(), System.currentTimeMillis() - time);
    }

    public Gui getPreviewGui() {
        return previewGui;
    }

    public OpenDetails getDetails() {
        return details;
    }

    public Player getViewer() {
        return viewer;
    }
}
