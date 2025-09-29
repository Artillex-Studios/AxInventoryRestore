package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public enum Save {
    INSTANCE;

    public void execute(CommandSender sender, Player player) {
        final String cause = MESSAGES.getString("manual-created-by").replace("%player%", sender.getName());

        AxInventoryRestore.getDatabase().saveInventory(player, "MANUAL", cause);
        BackupLimiter.tryLimit(player.getUniqueId(), "manual", "MANUAL");
        MESSAGEUTILS.sendLang(sender, "manual-backup", Map.of("%player%", player.getName()));
    }
}
