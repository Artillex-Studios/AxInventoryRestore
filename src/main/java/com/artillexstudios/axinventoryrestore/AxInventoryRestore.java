package com.artillexstudios.axinventoryrestore;

import com.artillexstudios.axinventoryrestore.commands.Commands;
import com.artillexstudios.axinventoryrestore.config.AbstractConfig;
import com.artillexstudios.axinventoryrestore.config.impl.Config;
import com.artillexstudios.axinventoryrestore.config.impl.Messages;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.database.impl.SQLite;
import com.artillexstudios.axinventoryrestore.listeners.RegisterListeners;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.plugin.java.JavaPlugin;

public final class AxInventoryRestore extends JavaPlugin {
    private static AbstractConfig abstractConfig;
    private static AbstractConfig abstractMessages;
    public static YamlDocument MESSAGES;
    public static YamlDocument CONFIG;
    private static AxInventoryRestore instance;
    private static Database database;

    public static AxInventoryRestore getInstance() {
        return instance;
    }

    public static Database getDatabase() {
        return database;
    }

    @Override
    public void onEnable() {
        instance = this;

        abstractConfig = new Config();
        abstractConfig.setup();
        CONFIG = abstractConfig.getConfig();

        abstractMessages = new Messages();
        abstractMessages.setup();
        MESSAGES = abstractMessages.getConfig();

        database = new SQLite();
        database.setup();

        new RegisterListeners().register();

        this.getCommand("axinventoryrestore").setExecutor(new Commands());

    }

    @Override
    public void onDisable() {
    }
}
