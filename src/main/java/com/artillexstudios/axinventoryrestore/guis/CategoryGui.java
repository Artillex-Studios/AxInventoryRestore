package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.utils.DateUtils;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class CategoryGui {
    private final PaginatedGui categoryGui;
    private final MainGui mainGui;
    private final Player viewer;
    private final UUID restoreUser;
    private final List<BackupData> backupDataList;
    private final PaginatedGui lastGui;
    private final int pageNum;
    private final int rows = CONFIG.getInt("menu-rows.backup-selector", 4);

    public CategoryGui(@NotNull MainGui mainGui, List<BackupData> backupDataList, PaginatedGui lastGui, int pageNum) {
        this.mainGui = mainGui;
        this.viewer = mainGui.getViewer();
        this.restoreUser = mainGui.getRestoreUser();
        this.backupDataList = backupDataList;
        this.lastGui = lastGui;
        this.pageNum = pageNum;

        categoryGui = Gui.paginated()
                .title(StringUtils.format(MESSAGES.getString("guis.categorygui.title").replace("%player%", mainGui.getName())))
                .rows(rows)
                .pageSize(rows * 9 - 9)
                .create();
    }

    public void open() {
        categoryGui.clearPageItems();

        final CategoryGui cGui = this;
        int n = 1;
        for (BackupData backupData : backupDataList) {
            final Map<String, String> replacements = new HashMap<>();

            replacements.put("%date%", DateUtils.formatDate(backupData.getDate()));
            replacements.put("%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()));
            replacements.put("%cause%", backupData.getCause() == null ? "---" : backupData.getCause());

            final ItemStack it = ItemBuilder.create(MESSAGES.getSection("guis.categorygui.item"), replacements).get();
            it.setAmount(n);

            categoryGui.addItem(new GuiItem(it, event -> {
                new PreviewGui(cGui, backupData, categoryGui, categoryGui.getCurrentPageNum()).open();
            }));

            n++;
            if (n > 64) {
                categoryGui.update();
                n = 1;
            }
        }
        categoryGui.update();

        // Previous item
        categoryGui.setItem(rows, 3, new GuiItem(ItemBuilder.create(MESSAGES.getSection("gui-items.previous-page")).get(), event2 -> categoryGui.previous()));
        // Next item
        categoryGui.setItem(rows, 7, new GuiItem(ItemBuilder.create(MESSAGES.getSection("gui-items.next-page")).get(), event2 -> categoryGui.next()));

        categoryGui.setDefaultClickAction(event -> event.setCancelled(true));

        categoryGui.setItem(rows, 5, new GuiItem(ItemBuilder.create(MESSAGES.getSection("gui-items.back")).get(), event2 -> {
            lastGui.open(viewer, pageNum);
        }));

        categoryGui.open(viewer);
    }

    public Player getViewer() {
        return viewer;
    }

    public UUID getRestoreUser() {
        return restoreUser;
    }

    public PaginatedGui getCategoryGui() {
        return categoryGui;
    }

    public MainGui getMainGui() {
        return mainGui;
    }
}
