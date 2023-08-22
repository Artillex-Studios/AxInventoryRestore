package com.artillexstudios.axinventoryrestore;

import com.artillexstudios.axinventoryrestore.commands.Commands;
import com.artillexstudios.axinventoryrestore.commands.TabComplete;
import com.artillexstudios.axinventoryrestore.config.AbstractConfig;
import com.artillexstudios.axinventoryrestore.config.impl.Config;
import com.artillexstudios.axinventoryrestore.config.impl.Messages;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.database.DatabaseQueue;
import com.artillexstudios.axinventoryrestore.database.impl.H2;
import com.artillexstudios.axinventoryrestore.database.impl.MySQL;
import com.artillexstudios.axinventoryrestore.database.impl.PostgreSQL;
import com.artillexstudios.axinventoryrestore.database.impl.SQLite;
import com.artillexstudios.axinventoryrestore.libraries.Libraries;
import com.artillexstudios.axinventoryrestore.listeners.RegisterListeners;
import com.artillexstudios.axinventoryrestore.schedulers.AutoBackupScheduler;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.byteflux.libby.BukkitLibraryManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AxInventoryRestore extends JavaPlugin {
    private static AbstractConfig abstractConfig;
    private static AbstractConfig abstractMessages;
    public static YamlDocument MESSAGES;
    public static YamlDocument CONFIG;
    private static AxInventoryRestore instance;
    private static DatabaseQueue databaseQueue;
    private static Database database;

    public static AbstractConfig getAbstractConfig() {
        return abstractConfig;
    }

    public static AbstractConfig getAbstractMessages() {
        return abstractMessages;
    }

    public static AxInventoryRestore getInstance() {
        return instance;
    }

    public static Database getDB() {
        return database;
    }

    public static DatabaseQueue getDatabaseQueue() {
        return databaseQueue;
    }

    @Override
    public void onLoad() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this, "libraries");
        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        libraryManager.addRepository("https://repo.codemc.org/repository/maven-public/");
        libraryManager.addRepository("https://repo.papermc.io/repository/maven-public/");

        for (Libraries lib : Libraries.values()) {
            libraryManager.loadLibrary(lib.getLibrary());
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        new ColorUtils();

        int pluginId = 19446;
        new Metrics(this, pluginId);

        abstractConfig = new Config();
        abstractConfig.setup();
        CONFIG = abstractConfig.getConfig();

        abstractMessages = new Messages();
        abstractMessages.setup();
        MESSAGES = abstractMessages.getConfig();

        databaseQueue = new DatabaseQueue("AxInventoryRestore-Datastore-thread");

        switch (CONFIG.getString("database.type").toLowerCase()) {
            case "h2":
                database = new H2();
                break;
            case "mysql":
                database = new MySQL();
                break;
            case "postgresql":
                database = new PostgreSQL();
                break;
            default:
                database = new SQLite();
                break;
        }

        database.setup();
        database.cleanup();
        new RegisterListeners().register();

        this.getCommand("axinventoryrestore").setExecutor(new Commands());
        this.getCommand("axinventoryrestore").setTabCompleter(new TabComplete());

        new AutoBackupScheduler().start();
    }

    @Override
    public void onDisable() {
        database.cleanup();
    }
}
