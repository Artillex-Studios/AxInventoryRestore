package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RestoreGui {
    private final PaginatedGui restoreGui;
    private final MainGui mainGui;
    private final Player viewer;
    private final OfflinePlayer restoreUser;
    private final SaveReason saveReason;

    public RestoreGui(@NotNull MainGui mainGui, SaveReason saveReason) {
        this.mainGui = mainGui;
        this.viewer = mainGui.getViewer();
        this.restoreUser = mainGui.getRestoreUser();
        this.saveReason = saveReason;

        restoreGui = Gui.paginated()
                .title(ColorUtils.deserialize("<black><b>Restore ></b></black> <dark_gray>" + restoreUser.getName()))
                .rows(4)
                .pageSize(27)
                .create();
    }

    public void openRestoreGui() {
        restoreGui.clearPageItems();

        for (BackupData backupData : AxInventoryRestore.getDatabase().getDeathsByType(restoreUser, saveReason)) {
            if (backupData.getItems() == null) continue;
            restoreGui.addItem(ItemBuilder.from(Material.BARREL).name(ColorUtils.deserialize("<!i>&#FFFF00&l" + backupData.getItems().length)).asGuiItem(event -> {
                // open gui
            }));
        }

        // Previous item
        restoreGui.setItem(4, 3, ItemBuilder.from(Material.ARROW).name(ColorUtils.deserialize("<!i>&#FF3333&lPrevious page")).asGuiItem(event2 -> restoreGui.previous()));
        // Next item
        restoreGui.setItem(4, 7, ItemBuilder.from(Material.ARROW).name(ColorUtils.deserialize("<!i>&#33FF33&lNext page")).asGuiItem(event2 -> restoreGui.next()));


        restoreGui.setDefaultClickAction(event -> event.setCancelled(true));

        restoreGui.setItem(4, 5, ItemBuilder.from(Material.BARRIER).name(ColorUtils.deserialize("<!i>&#FF0000&lBack")).asGuiItem(event2 -> {
            mainGui.getMainGui().open(viewer);
        }));

        restoreGui.open(viewer);
    }
}
