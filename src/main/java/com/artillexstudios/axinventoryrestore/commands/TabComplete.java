package com.artillexstudios.axinventoryrestore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter {
    final List<String> results = new ArrayList<>();

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        results.clear();

        if (args.length == 0 || (args.length == 1 && ("save".contains(args[0]) || "reload".contains(args[0]) || "cleanup".contains(args[0])))) {
            if ("save".contains(args[0]) && !args[0].equalsIgnoreCase("save")) {
                results.add("save");
            }

            if ("reload".contains(args[0]) && !args[0].equalsIgnoreCase("reload")) {
                results.add("reload");
            }

            if ("cleanup".contains(args[0]) && !args[0].equalsIgnoreCase("cleanup")) {
                results.add("cleanup");
            }
        }

        if (args.length == 1 && results.isEmpty()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().toLowerCase().contains(args[0].toLowerCase())) continue;

                results.add(p.getName());
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("save")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().toLowerCase().contains(args[1].toLowerCase())) continue;

                results.add(p.getName());
            }

            results.add("*");
        }

        return results;
    }
}