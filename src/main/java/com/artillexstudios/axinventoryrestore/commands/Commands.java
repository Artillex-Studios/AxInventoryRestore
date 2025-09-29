package com.artillexstudios.axinventoryrestore.commands;

import com.artillexstudios.axinventoryrestore.commands.subcommands.Cleanup;
import com.artillexstudios.axinventoryrestore.commands.subcommands.Help;
import com.artillexstudios.axinventoryrestore.commands.subcommands.Reload;
import com.artillexstudios.axinventoryrestore.commands.subcommands.Save;
import com.artillexstudios.axinventoryrestore.commands.subcommands.SaveAll;
import com.artillexstudios.axinventoryrestore.commands.subcommands.View;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"axinventoryrestore", "axir", "axinvrestore", "invrestore", "inventoryrestore"})
public class Commands {

    @DefaultFor({"~", "~ help"})
    public void help(@NotNull CommandSender sender) {
        Help.INSTANCE.execute(sender);
    }

    @Subcommand("view")
    @CommandPermission("axinventoryrestore.view")
    @AutoComplete("@offlinePlayers")
    public void view(Player sender, String player) {
        View.INSTANCE.execute(sender, player);
    }

    @Subcommand("reload")
    @CommandPermission("axinventoryrestore.reload")
    public void reload(CommandSender sender) {
        Reload.INSTANCE.execute(sender);
    }

    @Subcommand("cleanup")
    @CommandPermission("axinventoryrestore.cleanup")
    public void cleanup(CommandSender sender) {
        Cleanup.INSTANCE.execute(sender);
    }

    @Subcommand("save")
    @CommandPermission("axinventoryrestore.manualbackup")
    public void save(CommandSender sender, Player player) {
        Save.INSTANCE.execute(sender, player);
    }

    @Subcommand("saveall")
    @CommandPermission("axinventoryrestore.manualbackup")
    public void saveAll(CommandSender sender) {
        SaveAll.INSTANCE.execute(sender);
    }
}
