package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.command.CommandSender;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.LANG;

public enum Help {
    INSTANCE;

    public void execute(CommandSender sender) {
        for (String line : LANG.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(line));
        }
    }
}
