package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public enum SaveAll {
    INSTANCE;

    public void execute(CommandSender sender) {
        final String cause = MESSAGES.getString("manual-created-by").replace("%player%", sender.getName());

        for (Player pl : Bukkit.getOnlinePlayers()) {
            AxInventoryRestore.getDatabase().saveInventory(pl, "MANUAL", cause);
            BackupLimiter.tryLimit(pl.getUniqueId(), "manual", "MANUAL");
        }

        MESSAGEUTILS.sendLang(sender, "manual-backup-all");
    }
}
