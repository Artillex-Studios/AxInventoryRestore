package com.artillexstudios.axinventoryrestore;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axinventoryrestore.queue.PriorityThreadedQueue;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.reflection.FastFieldAccessor;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axinventoryrestore.commands.Commands;
import com.artillexstudios.axinventoryrestore.database.Database;
import com.artillexstudios.axinventoryrestore.database.impl.H2;
import com.artillexstudios.axinventoryrestore.database.impl.MySQL;
import com.artillexstudios.axinventoryrestore.database.impl.PostgreSQL;
import com.artillexstudios.axinventoryrestore.discord.DiscordAddon;
import com.artillexstudios.axinventoryrestore.events.WebHooks;
import com.artillexstudios.axinventoryrestore.libraries.Libraries;
import com.artillexstudios.axinventoryrestore.listeners.RegisterListeners;
import com.artillexstudios.axinventoryrestore.schedulers.AutoBackupScheduler;
import com.artillexstudios.axinventoryrestore.utils.CommandMessages;
import com.artillexstudios.axinventoryrestore.utils.UpdateNotifier;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Warning;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.io.File;
import java.util.Locale;

public final class AxInventoryRestore extends AxPlugin {
    public static Config CONFIG;
    public static Config MESSAGES;
    public static Config DISCORD;
    public static MessageUtils MESSAGEUTILS;
    private static AxInventoryRestore instance;
    private static PriorityThreadedQueue<Runnable> threadedQueue;
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

    public static PriorityThreadedQueue<Runnable> getThreadedQueue() {
        return threadedQueue;
    }

    public void load() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this, "lib");
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

        int pluginId = 19446;
        final Metrics metrics = new Metrics(this, pluginId);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        MESSAGES = new Config(new File(getDataFolder(), "messages.yml"), getResource("messages.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        DISCORD = new Config(new File(getDataFolder(), "discord.yml"), getResource("discord.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());

        WebHooks.reload();
        threadedQueue = new PriorityThreadedQueue<>("AxInventoryRestore-Datastore-thread");

        MESSAGEUTILS = new MessageUtils(MESSAGES.getBackingDocument(), "prefix", CONFIG.getBackingDocument());

        switch (CONFIG.getString("database.type").toLowerCase()) {
            case "mysql":
                database = new MySQL();
                break;
            case "postgresql":
                database = new PostgreSQL();
                break;
            default:
                database = new H2();
                break;
        }

        metrics.addCustomChart(new SimplePie("database_type", () -> database.getType()));

        database.setup();
        AxInventoryRestore.getThreadedQueue().submit(() -> database.cleanup());
        new RegisterListeners().register();

        Warning.WarningState prevState = Bukkit.getWarningState();
        FastFieldAccessor accessor = FastFieldAccessor.forClassField(Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer", "warningState");
        accessor.set(Bukkit.getServer(), Warning.WarningState.OFF);
        final BukkitCommandHandler handler = BukkitCommandHandler.create(instance);
        accessor.set(Bukkit.getServer(), prevState);

        handler.getAutoCompleter().registerSuggestion("offlinePlayers", (args, sender, command) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());

        handler.getTranslator().add(new CommandMessages());
        handler.setLocale(new Locale("en", "US"));

        handler.register(new Commands());

        AutoBackupScheduler.start();

        boolean loadDiscordAddon = CONFIG.getBoolean("enable-discord-addon", false);
        if (loadDiscordAddon && !DISCORD.getString("token").isBlank()) discordAddon = new DiscordAddon();
        metrics.addCustomChart(new SimplePie("uses_discord_addon", () -> "" + loadDiscordAddon));

        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 4610);
    }

    public void disable() {
        AutoBackupScheduler.stop();
        threadedQueue.stop();
        database.disable();
    }
}
