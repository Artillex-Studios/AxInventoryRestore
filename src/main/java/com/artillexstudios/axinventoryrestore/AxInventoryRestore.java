package com.artillexstudios.axinventoryrestore;

import com.alessiodp.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.data.ThreadedQueue;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axinventoryrestore.commands.Commands;
import com.artillexstudios.axinventoryrestore.commands.TabComplete;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.database.impl.H2;
import com.artillexstudios.axinventoryrestore.database.impl.MySQL;
import com.artillexstudios.axinventoryrestore.database.impl.PostgreSQL;
import com.artillexstudios.axinventoryrestore.database.impl.SQLite;
import com.artillexstudios.axinventoryrestore.discord.DiscordAddon;
import com.artillexstudios.axinventoryrestore.events.WebHooks;
import com.artillexstudios.axinventoryrestore.libraries.Libraries;
import com.artillexstudios.axinventoryrestore.listeners.RegisterListeners;
import com.artillexstudios.axinventoryrestore.schedulers.AutoBackupScheduler;
import com.artillexstudios.axinventoryrestore.utils.ColorUtils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class AxInventoryRestore extends AxPlugin {
    public static Config CONFIG;
    public static Config MESSAGES;
    public static Config DISCORD;
    private static AxInventoryRestore instance;
    private static ThreadedQueue<Runnable> threadedQueue;
    private static Database database;
    private static DiscordAddon discordAddon = null;

    @Nullable
    public static DiscordAddon getDiscordAddon() {
        return discordAddon;
    }

    public static AxInventoryRestore getInstance() {
        return instance;
    }

    public static Database getDB() {
        return database;
    }

    public static ThreadedQueue<Runnable> getThreadedQueue() {
        return threadedQueue;
    }

    public void load() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this, "libraries");
        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        libraryManager.addRepository("https://repo.codemc.org/repository/maven-public/");
        libraryManager.addRepository("https://repo.papermc.io/repository/maven-public/");

        for (Libraries lib : Libraries.values()) {
            libraryManager.loadLibrary(lib.getLibrary());
        }
    }

    public void enable() {
        instance = this;

        new ColorUtils();

        int pluginId = 19446;
        final Metrics metrics = new Metrics(this, pluginId);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        MESSAGES = new Config(new File(getDataFolder(), "messages.yml"), getResource("messages.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        DISCORD = new Config(new File(getDataFolder(), "discord.yml"), getResource("discord.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());

        WebHooks.reload();
        threadedQueue = new ThreadedQueue<>("AxInventoryRestore-Datastore-thread");

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

        metrics.addCustomChart(new SimplePie("database_type", () -> database.getType()));

        database.setup();
        database.cleanup();
        new RegisterListeners().register();

        this.getCommand("axinventoryrestore").setExecutor(new Commands());
        this.getCommand("axinventoryrestore").setTabCompleter(new TabComplete());

        new AutoBackupScheduler().start();

        boolean loadDiscordAddon = CONFIG.getBoolean("enable-discord-addon", false);
        if (loadDiscordAddon) discordAddon = new DiscordAddon();
        metrics.addCustomChart(new SimplePie("uses_discord_addon", () -> "" + loadDiscordAddon));
    }

    public void disable() {
        database.cleanup();
        database.disable();
    }
}
