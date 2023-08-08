package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axinventoryrestore.enums.SaveReason;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MainGui {
    private final PaginatedGui mainGui;
    private final Player viewer;
    private final OfflinePlayer restoreUser;

    public MainGui(@NotNull OfflinePlayer restoreUser, @NotNull Player viewer) {
        this.viewer = viewer;
        this.restoreUser = restoreUser;

        mainGui = Gui.paginated()
                .title(ColorUtils.deserialize("<black><b>Restore ></b></black> <dark_gray>" + restoreUser.getName()))
                .rows(4)
                .pageSize(27)
                .create();
    }

    public void openMainGui() {
        mainGui.clearPageItems();

        // Previous item
        mainGui.setItem(4, 3, ItemBuilder.from(Material.ARROW).name(ColorUtils.deserialize("<!i>&#FF3333&lPrevious page")).asGuiItem(event2 -> mainGui.previous()));
        // Next item
        mainGui.setItem(4, 7, ItemBuilder.from(Material.ARROW).name(ColorUtils.deserialize("<!i>&#33FF33&lNext page")).asGuiItem(event2 -> mainGui.next()));

        for (SaveReason saveReason : SaveReason.values()) {
            mainGui.addItem(ItemBuilder.from(Material.PAPER).name(ColorUtils.deserialize("<!i>&#FFFF00&l" + saveReason.toString())).asGuiItem(event -> {
                new RestoreGui(this, saveReason).openRestoreGui();
            }));
        }

        mainGui.setDefaultClickAction(event -> event.setCancelled(true));

        mainGui.setItem(4, 5, ItemBuilder.from(Material.BARRIER).name(ColorUtils.deserialize("<!i>&#FF0000&lClose")).asGuiItem(event2 -> {
            mainGui.close(viewer);
        }));

        mainGui.open(viewer);
    }

    public PaginatedGui getMainGui() {
        return mainGui;
    }

    public OfflinePlayer getRestoreUser() {
        return restoreUser;
    }

    public Player getViewer() {
        return viewer;
    }
}
