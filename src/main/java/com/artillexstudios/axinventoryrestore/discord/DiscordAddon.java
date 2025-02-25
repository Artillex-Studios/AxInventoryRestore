package com.artillexstudios.axinventoryrestore.discord;

import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.backups.BackupData;
import com.artillexstudios.axinventoryrestore.utils.JDAEmbedBuilder;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.DISCORD;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class DiscordAddon extends ListenerAdapter {
    private JDA jda = null;

    public DiscordAddon() {
        if (DISCORD.getString("token").isBlank()) return;
        final JDABuilder jdaBuilder = JDABuilder.createDefault(DISCORD.getString("token"));
        jdaBuilder.setActivity(Activity.playing(DISCORD.getString("bot-activity", " ")));

        jda = jdaBuilder.build();
        try {
            jda.awaitReady();
            jda.addEventListener(this);
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FF00[AxInventoryRestore] Loaded discord module!"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendRequest(final Player requester, final BackupData backupData) {
        final TextChannel channel = jda.getTextChannelById(DISCORD.getString("channel-id"));
        if (channel == null) {
            Bukkit.getLogger().warning("Discord channel with id " + DISCORD.getString("channel-id") + " was not found!");
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

        if (ClassUtils.INSTANCE.classExists("net.luckperms.api.LuckPerms")) {
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

        final MessageCreateAction action = channel.sendMessageEmbeds(new JDAEmbedBuilder(DISCORD.getSection("prompt"), replacements).get());
        action.addActionRow(
            Button.success("axir-accept:" + id, DISCORD.getString("messages.restore")),
            Button.danger("axir-deny:" + id, DISCORD.getString("messages.decline")))
        .queue((message -> {
            if (!DISCORD.getBoolean("create-thread", true)) return;
            channel.createThreadChannel(DISCORD.getString("thread-name", "-"), message.getId()).queue();
        }));

    }

    public ItemStack getRequestItem() {
        return new ItemBuilder(DISCORD.getSection("request-restore")).get();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String status;
        if (event.getComponentId().startsWith("axir-accept")) {
            status = "accepted";
            AxInventoryRestore.getDB().grantRestoreRequest(Integer.parseInt(event.getComponentId().split(":")[1]));
        }
        else if (event.getComponentId().startsWith("axir-deny")) {
            status = "declined";
            AxInventoryRestore.getDB().removeRestoreRequest(Integer.parseInt(event.getComponentId().split(":")[1]));
        } else return;

        if (event.getMember() == null) {
            event.reply("Something went wrong! member = null").setEphemeral(true).queue();
            return;
        }
        if (!event.getMember().hasPermission(Permission.valueOf(DISCORD.getString("required-permission", "ADMINISTRATOR")))) {
            event.reply(DISCORD.getString("messages.no-permission")).setEphemeral(true).queue();
            return;
        }

        try {
            event.deferReply().queue(interactionHook -> {
                final MessageEmbed embed = event.getMessage().getEmbeds().get(0);
                event.getMessage().editMessageEmbeds(net.dv8tion.jda.api.EmbedBuilder.fromData(embed.toData())
                                .setAuthor(event.getUser().getName(), null, event.getUser().getAvatarUrl())
                                .setColor(Integer.parseInt(DISCORD.getString("messages." + status +"-color").replace("#", ""), 16)).build())
                        .queue();
                event.getMessage().editMessageComponents().queue();
                interactionHook.sendMessage((DISCORD.getString("messages." + status))).setEphemeral(true).queue();
            });
        } catch (Exception ex) {
            // ignore jda's spam if interaction fails
        }
    }
}
