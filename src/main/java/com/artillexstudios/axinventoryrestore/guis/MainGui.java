package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.backups.Backup;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.queue.Priority;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public class MainGui {
    private final PaginatedGui mainGui;
    private final Player viewer;
    private final UUID restoreUser;
    private final String name;
    private final int rows = CONFIG.getInt("menu-rows.main-menu", 4);

    public MainGui(@NotNull UUID restoreUser, @NotNull Player viewer, String name) {
        this.viewer = viewer;
        this.restoreUser = restoreUser;
        this.name = name;

        mainGui = Gui.paginated()
                .title(StringUtils.format(MESSAGES.getString("guis.maingui.title").replace("%player%", name)))
                .rows(rows)
                .pageSize(rows * 9 - 9)
                .create();
    }

    public void openMainGui() {
        mainGui.clearPageItems();

        AxInventoryRestore.getThreadedQueue().submit(() -> {
            final Backup backup = AxInventoryRestore.getDB().getBackupsOfPlayer(restoreUser);
            final ArrayList<String> reasons = new ArrayList<>();
            if (CONFIG.getBoolean("enable-all-category")) reasons.add("ALL");
            reasons.addAll(backup.getDeathsPerTypes().keySet());

            if ((CONFIG.getBoolean("enable-all-category") && reasons.size() == 1) || reasons.isEmpty()) {
                MESSAGEUTILS.sendLang(viewer, "errors.unknown-player", Map.of("%number%", "3"));
                Scheduler.get().runAt(viewer.getLocation(), t -> viewer.closeInventory());
                return;
            }

            for (String saveReason : reasons) {
                ItemStack item = new ItemBuilder(Material.PAPER).setName(StringUtils.formatToString("<!i>&#FFFF00&l" + saveReason)).get();

                final List<BackupData> backupDataList = backup.getDeathsByReason(saveReason);

                if (MESSAGES.getSection("categories." + saveReason) != null) {
                    item = new ItemBuilder(MESSAGES.getSection("categories." + saveReason), Map.of("%amount%", "" + backupDataList.size())).get();
                }

                mainGui.addItem(new GuiItem(item, event ->
                        Scheduler.get().runAt(viewer.getLocation(), task ->
                                new CategoryGui(this, backupDataList, mainGui, mainGui.getCurrentPageNum()).openCategoryGui())));

                mainGui.update();
            }
        }, Priority.HIGH);

        // Previous item
        mainGui.setItem(rows, 3, new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.previous-page")).get(), event2 -> mainGui.previous()));
        // Next item
        mainGui.setItem(rows, 7, new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.next-page")).get(), event2 -> mainGui.next()));

        mainGui.setDefaultClickAction(event -> event.setCancelled(true));

        mainGui.setItem(rows, 5, new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.close")).get(), event2 -> {
            mainGui.close(viewer);
        }));

        mainGui.open(viewer);
    }

    public PaginatedGui getMainGui() {
        return mainGui;
    }

    public UUID getRestoreUser() {
        return restoreUser;
    }

    public Player getViewer() {
        return viewer;
    }

    public String getName() {
        return name;
    }
}
