package com.artillexstudios.axinventoryrestore.commands;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.utils.CommandMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.Locale;

public class CommandManager {
    private static BukkitCommandHandler handler = null;

    public static void load() {
        handler = BukkitCommandHandler.create(AxInventoryRestore.getInstance());

        handler.getAutoCompleter().registerSuggestion("offlinePlayers", (args, sender, command) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());

        handler.getTranslator().add(new CommandMessages());
        handler.setLocale(Locale.of("en", "US"));

        reload();
    }

    public static void reload() {
        handler.unregisterAllCommands();

        handler.register(new Commands());

        handler.registerBrigadier();
    }
}
