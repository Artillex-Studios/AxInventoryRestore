package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.search.OpenDetails;
import com.artillexstudios.axinventoryrestore.utils.DateUtils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.LANG;

public class CategoryGui {
    private final OpenDetails details;
    private final PaginatedGui categoryGui;
    private final Player viewer;
    private final List<BackupData> backupDataList;
    private final PaginatedGui lastGui;
    private final int pageNum;
    private final int rows = CONFIG.getInt("menu-rows.backup-selector", 4);

    public CategoryGui(OpenDetails details, Player viewer, List<BackupData> backupDataList, PaginatedGui lastGui, int pageNum) {
        this.details = details;
        this.viewer = viewer;
        this.backupDataList = backupDataList;
        this.lastGui = lastGui;
        this.pageNum = pageNum;

        categoryGui = Gui.paginated()
                .title(StringUtils.format(LANG.getString("guis.categorygui.title").replace("%player%", details.getName())))
                .rows(rows)
                .pageSize(rows * 9 - 9)
                .create();
    }

    public void open() {
        categoryGui.clearPageItems();

        int n = 1;
        for (BackupData backupData : backupDataList) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("%player%", backupData.getPlayerName());
            replacements.put("%category%", LANG.getString("categories." + backupData.getReason() + ".raw", backupData.getReason()));
            replacements.put("%date%", DateUtils.formatDate(backupData.getDate()));
            replacements.put("%location%", backupData.getLocation().getReadable());
            replacements.put("%cause%", backupData.getCause() == null ? "---" : backupData.getCause());

            final ItemStack it = ItemBuilder.create(LANG.getSection("guis.categorygui.item"), replacements).get();
            it.setAmount(n);

            categoryGui.addItem(new GuiItem(it, event -> {
                new PreviewGui(details, viewer, backupData, categoryGui, categoryGui.getCurrentPageNum()).open();
            }));

            n++;
            if (n > 64) {
                categoryGui.update();
                n = 1;
            }
        }
        categoryGui.update();

        // Previous item
        categoryGui.setItem(rows, 3, new GuiItem(ItemBuilder.create(LANG.getSection("gui-items.previous-page")).get(), event2 -> categoryGui.previous()));
        // Next item
        categoryGui.setItem(rows, 7, new GuiItem(ItemBuilder.create(LANG.getSection("gui-items.next-page")).get(), event2 -> categoryGui.next()));

        categoryGui.setDefaultClickAction(event -> event.setCancelled(true));

        if (lastGui != null) {
            categoryGui.setItem(rows, 5, new GuiItem(ItemBuilder.create(LANG.getSection("gui-items.back")).get(), event2 -> {
                lastGui.open(viewer, pageNum);
            }));
        }

        categoryGui.open(viewer);
    }

    public PaginatedGui getCategoryGui() {
        return categoryGui;
    }

    public OpenDetails getDetails() {
        return details;
    }

    public Player getViewer() {
        return viewer;
    }
}
