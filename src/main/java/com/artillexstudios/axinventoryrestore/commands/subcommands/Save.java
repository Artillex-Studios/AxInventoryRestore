package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.LANG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public enum Save {
    INSTANCE;

    public void execute(CommandSender sender, Player player) {
        final String cause = LANG.getString("manual-created-by").replace("%player%", sender.getName());

        AxInventoryRestore.getDatabase().saveInventory(player, "MANUAL", cause);
        MESSAGEUTILS.sendLang(sender, "manual-backup", Map.of("%player%", player.getName()));
    }
}
