package com.artillexstudios.axinventoryrestore.discord;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.ClassUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.utils.EmbedBuilder;
import com.artillexstudios.axinventoryrestore.utils.ItemBuilder;
import com.artillexstudios.axinventoryrestore.utils.LocationUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class DiscordAddon extends ListenerAdapter {
    private JDA jda = null;
    public Config DISCORDCONFIG;

    public DiscordAddon() {
        DISCORDCONFIG = new Config(new File(AxInventoryRestore.getInstance().getDataFolder(), "discord.yml"), AxInventoryRestore.getInstance().getResource("discord.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());

        if (DISCORDCONFIG.getString("token").isBlank()) return;
        final JDABuilder jdaBuilder = JDABuilder.createDefault(DISCORDCONFIG.getString("token"));
        jdaBuilder.setActivity(Activity.playing(DISCORDCONFIG.getString("bot-activity", " ")));

        jda = jdaBuilder.build();
        try {
            jda.awaitReady();
            jda.addEventListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendRequest(final Player requester, final BackupData backupData) {
        final TextChannel channel = jda.getTextChannelById(DISCORDCONFIG.getString("channel-id"));
        if (channel == null) {
            Bukkit.getLogger().warning("Discord channel with id " + DISCORDCONFIG.getString("channel-id") + " was not found!");
            return;
        }

        int id = AxInventoryRestore.getDB().addRestoreRequest(backupData.getId());

        final HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%player%", Bukkit.getOfflinePlayer(backupData.getPlayerUUID()).getName());
        replacements.put("%requester%", requester.getName());
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date resultdate = new Date(backupData.getDate());
        replacements.put("%date%", sdf.format(resultdate));
        replacements.put("%category%", MESSAGES.getString("categories." + backupData.getReason() + ".raw", "---"));
        replacements.put("%cause%", backupData.getCause() == null ? "---" : backupData.getCause());
        replacements.put("%location%", LocationUtils.serializeLocationReadable(backupData.getLocation()));


        if (ClassUtils.classExists("net.luckperms.api.LuckPerms")) {
            final RegisteredServiceProvider<net.luckperms.api.LuckPerms> provider = Bukkit.getServicesManager().getRegistration(net.luckperms.api.LuckPerms.class);
            if (provider != null) {
                final net.luckperms.api.LuckPerms api = provider.getProvider();

                final net.luckperms.api.context.ImmutableContextSet set = api.getContextManager().getStaticContext();
                if (set.getAnyValue("server").isPresent()) {
                    final String str = set.getAnyValue("server").get();
                    replacements.put("%server%", str);
                }
            }
        }

        final MessageCreateAction action = channel.sendMessageEmbeds(new EmbedBuilder(DISCORDCONFIG.getSection("prompt"), replacements).get());
        action.addActionRow(
            Button.success("axir-accept:" + id, DISCORDCONFIG.getString("messages.restore")),
            Button.danger("axir-deny:" + id, DISCORDCONFIG.getString("messages.decline")))
        .queue((message -> {
            if (!DISCORDCONFIG.getBoolean("create-thread", true)) return;
            channel.createThreadChannel(DISCORDCONFIG.getString("thread-name", "-"), message.getId()).queue();
        }));

    }

    public ItemStack getRequestItem() {
        return new ItemBuilder(DISCORDCONFIG, "request-restore", Map.of()).getItem();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String status = null;
        if (event.getComponentId().startsWith("axir-accept")) {
            status = "accepted";
            AxInventoryRestore.getDB().grantRestoreRequest(Integer.parseInt(event.getComponentId().split(":")[1]));
        }
        else if (event.getComponentId().startsWith("axir-deny")) {
            status = "declined";
            AxInventoryRestore.getDB().removeRestoreRequest(Integer.parseInt(event.getComponentId().split(":")[1]));
        }

        if (event.getMember() == null) return;
        if (!event.getMember().hasPermission(Permission.valueOf(DISCORDCONFIG.getString("required-permission", "ADMINISTRATOR")))) {
            event.reply(DISCORDCONFIG.getString("messages.no-permission")).setEphemeral(true).queue();
            return;
        }

        final MessageEmbed embed = event.getMessage().getEmbeds().get(0);
        event.getMessage().editMessageEmbeds(net.dv8tion.jda.api.EmbedBuilder.fromData(embed.toData())
                .setAuthor(event.getUser().getName(), null, event.getUser().getAvatarUrl())
                .setColor(Integer.parseInt(DISCORDCONFIG.getString("messages." + status +"-color").replace("#", ""), 16)).build())
                .queue();
        event.getMessage().editMessageComponents().queue();
        event.reply(DISCORDCONFIG.getString("messages." + status)).setEphemeral(true).queue();
    }
}
