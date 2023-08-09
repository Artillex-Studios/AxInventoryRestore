package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CategoryGui {
    private final PaginatedGui categoryGui;
    private final MainGui mainGui;
    private final Player viewer;
    private final OfflinePlayer restoreUser;
    private final SaveReason saveReason;

    public CategoryGui(@NotNull MainGui mainGui, SaveReason saveReason) {
        this.mainGui = mainGui;
        this.viewer = mainGui.getViewer();
        this.restoreUser = mainGui.getRestoreUser();
        this.saveReason = saveReason;

        categoryGui = Gui.paginated()
                .title(ColorUtils.deserialize(AxInventoryRestore.MESSAGES.getString("guis.categorygui.title").replace("%player%", restoreUser.getName() == null ? "" + restoreUser.getUniqueId() : restoreUser.getName())))
                .rows(4)
                .pageSize(27)
                .create();
    }

    public void openCategoryGui() {
        categoryGui.clearPageItems();

        int n = 1;
        for (BackupData backupData : AxInventoryRestore.getDatabase().getDeathsByType(restoreUser, saveReason)) {
            if (backupData.getItems() == null) continue;

            final Map<String, String> replacements = new HashMap<>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date resultdate = new Date(backupData.getDate());
            replacements.put("%date%", sdf.format(resultdate));
            replacements.put("%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()));
            replacements.put("%cause%", backupData.getCause());

            final ItemStack it = new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "guis.categorygui.item", replacements).getItem();

            categoryGui.addItem(ItemBuilder.from(it).amount(n).asGuiItem(event -> {
                new PreviewGui(this, backupData).openPreviewGui();
            }));

            n++;

            if (n > 64) n = 1;
        }

        // Previous item
        categoryGui.setItem(4, 3, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "gui-items.previous-page", Map.of()).getItem()).asGuiItem(event2 -> categoryGui.previous()));
        // Next item
        categoryGui.setItem(4, 7, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "gui-items.next-page", Map.of()).getItem()).asGuiItem(event2 -> categoryGui.next()));


        categoryGui.setDefaultClickAction(event -> event.setCancelled(true));

        categoryGui.setItem(4, 5, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "gui-items.back", Map.of()).getItem()).asGuiItem(event2 -> {
            mainGui.getMainGui().open(viewer);
        }));

        categoryGui.open(viewer);
    }

    public Player getViewer() {
        return viewer;
    }

    public OfflinePlayer getRestoreUser() {
        return restoreUser;
    }

    public PaginatedGui getCategoryGui() {
        return categoryGui;
    }

    public MainGui getMainGui() {
        return mainGui;
    }
}
