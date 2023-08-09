package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.MessageUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class MainGui {
    private final PaginatedGui mainGui;
    private final Player viewer;
    private final OfflinePlayer restoreUser;

    public MainGui(@NotNull OfflinePlayer restoreUser, @NotNull Player viewer) {
        this.viewer = viewer;
        this.restoreUser = restoreUser;

        mainGui = Gui.paginated()
                .title(ColorUtils.formatToComponent(AxInventoryRestore.MESSAGES.getString("guis.maingui.title").replace("%player%", restoreUser.getName() == null ? "" + restoreUser.getUniqueId() : restoreUser.getName())))
                .rows(4)
                .pageSize(27)
                .create();
    }

    public void openMainGui() {
        final ArrayList<String> reasons = AxInventoryRestore.getDB().getDeathReasons(restoreUser);
        if (reasons.isEmpty()) {
            MessageUtils.sendMsgP(viewer, "errors.unknown-player");
            return;
        }

        mainGui.clearPageItems();

        // Previous item
        mainGui.setItem(4, 3, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "gui-items.previous-page", Map.of()).getItem()).asGuiItem(event2 -> mainGui.previous()));
        // Next item
        mainGui.setItem(4, 7, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "gui-items.next-page", Map.of()).getItem()).asGuiItem(event2 -> mainGui.next()));

        for (String saveReason : reasons) {
            ItemBuilder item = ItemBuilder.from(Material.PAPER).name(ColorUtils.formatToComponent("<!i>&#FFFF00&l" + saveReason));

            if (AxInventoryRestore.MESSAGES.isSection("categories." + saveReason)) {
                item = ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "categories." + saveReason, Map.of("%amount%", "" + AxInventoryRestore.getDB().getDeathsByType(restoreUser, saveReason).size())).getItem());
            }

            mainGui.addItem(item.asGuiItem(event -> {
                new CategoryGui(this, saveReason).openCategoryGui();
            }));
        }

        mainGui.setDefaultClickAction(event -> event.setCancelled(true));

        mainGui.setItem(4, 5, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(AxInventoryRestore.MESSAGES, "gui-items.close", Map.of()).getItem()).asGuiItem(event2 -> {
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
