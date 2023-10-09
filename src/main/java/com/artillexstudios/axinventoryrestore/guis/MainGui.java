package com.artillexstudios.axinventoryrestore.guis;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupData;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import com.artillexstudios.axinventoryrestore.utils.MessageUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class MainGui {
    private final PaginatedGui mainGui;
    private final Player viewer;
    private final UUID restoreUser;
    private final String name;

    public MainGui(@NotNull UUID restoreUser, @NotNull Player viewer, String name) {
        this.viewer = viewer;
        this.restoreUser = restoreUser;
        this.name = name;

        mainGui = Gui.paginated()
                .title(ColorUtils.formatToComponent(MESSAGES.getString("guis.maingui.title").replace("%player%", name)))
                .rows(4)
                .pageSize(27)
                .create();
    }

    public void openMainGui() {
        mainGui.clearPageItems();

        final ArrayList<String> reasons = new ArrayList<>();

        if (CONFIG.getBoolean("enable-all-category"))
            reasons.add("ALL");

        reasons.addAll(AxInventoryRestore.getDB().getDeathReasons(restoreUser));

        if (reasons.size() == 1) {
            MessageUtils.sendMsgP(viewer, "errors.unknown-player");
            return;
        }

        AxInventoryRestore.getDatabaseQueue().submit(() -> {

            for (String saveReason : reasons) {
                ItemBuilder item = ItemBuilder.from(Material.PAPER).name(ColorUtils.formatToComponent("<!i>&#FFFF00&l" + saveReason));

                if (MESSAGES.isSection("categories." + saveReason)) {
                    item = ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "categories." + saveReason, Map.of("%amount%", "???")).getItem());
                }

                final ArrayList<BackupData> backupDataList = AxInventoryRestore.getDB().getDeathsByType(restoreUser, saveReason);

                final GuiItem gitem = item.asGuiItem(event -> {
                    new CategoryGui(this, saveReason, backupDataList, mainGui.getCurrentPageNum()).openCategoryGui();
                });

                gitem.setItemStack(ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "categories." + saveReason, Map.of("%amount%", "" + AxInventoryRestore.getDB().getDeathsSizeType(restoreUser, saveReason))).getItem()).build());
                mainGui.addItem(gitem);
                mainGui.update();
            }
        });

        // Previous item
        mainGui.setItem(4, 3, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "gui-items.previous-page", Map.of()).getItem()).asGuiItem(event2 -> mainGui.previous()));
        // Next item
        mainGui.setItem(4, 7, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "gui-items.next-page", Map.of()).getItem()).asGuiItem(event2 -> mainGui.next()));

        mainGui.setDefaultClickAction(event -> event.setCancelled(true));

        mainGui.setItem(4, 5, ItemBuilder.from(new com.artillexstudios.axinventoryrestore.utils.ItemBuilder(MESSAGES, "gui-items.close", Map.of()).getItem()).asGuiItem(event2 -> {
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
