package com.artillexstudios.axinventoryrestore.events;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axinventoryrestore.utils.WebhookEmbedBuilder;

import java.time.Instant;
import java.util.Map;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.DISCORD;

public class WebHooks {
    private static WebhookClient client = null;

    public static void reload() {
        String url = DISCORD.getString("url", "");
        if (url.isBlank()) {
            client = null;
            return;
        }
        client = WebhookClient.withUrl(url);
    }

    public static void sendBackupWebHook(Map<String, String> replacements) {
        if (client == null) return;
        send(getWebHook(DISCORD.getSection("backup-create"), replacements));
    }

    public static void sendRestoreWebHook(Map<String, String> replacements) {
        if (client == null) return;
        send(getWebHook(DISCORD.getSection("backup-restore"), replacements));
    }

    public static void sendExportWebHook(Map<String, String> replacements) {
        if (client == null) return;
        send(getWebHook(DISCORD.getSection("backup-export"), replacements));
    }

    private static WebhookMessage getWebHook(final Section section, Map<String, String> replacements) {
        if (section == null || !section.getBoolean("enabled", false)) return null;
        WebhookEmbed webhookEmbed = new WebhookEmbedBuilder(section, replacements).setTimeSpan(Instant.now()).get();
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setContent(section.getString("content"));
        builder.addEmbeds(webhookEmbed);
        return builder.build();
    }

    private static void send(WebhookMessage webhookMessage) {
        if (webhookMessage == null) return;
        client.send(webhookMessage);
    }
}
