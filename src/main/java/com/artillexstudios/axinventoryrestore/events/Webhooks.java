package com.artillexstudios.axinventoryrestore.events;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axdiscordwebhooks.builder.WebhookBuilder;
import com.artillexstudios.axdiscordwebhooks.webhook.Webhook;
import com.artillexstudios.axdiscordwebhooks.webhook.WebhookClient;

import java.util.HashMap;
import java.util.Map;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.DISCORD;

public class Webhooks {
    private static WebhookClient client = null;

    public static void reload() {
        stop();
        String url = DISCORD.getString("url", "");
        if (url.isBlank()) return;
        client = new WebhookClient(url);
    }

    public static void sendBackupWebhook(Map<String, String> replacements) {
        if (client == null) return;
        send(getWebhook(DISCORD.getSection("backup-create"), replacements));
    }

    public static void sendRestoreWebhook(Map<String, String> replacements) {
        if (client == null) return;
        send(getWebhook(DISCORD.getSection("backup-restore"), replacements));
    }

    public static void sendExportWebhook(Map<String, String> replacements) {
        if (client == null) return;
        send(getWebhook(DISCORD.getSection("backup-export"), replacements));
    }

    private static Webhook getWebhook(final Section section, Map<String, String> replacements) {
        if (section == null || !section.getBoolean("enabled", false)) return null;
        replacements = new HashMap<>(replacements);
        replacements.put("%timestamp%", String.valueOf(System.currentTimeMillis()));
        WebhookBuilder builder = WebhookBuilder.create(section, replacements);
        return builder.get();
    }

    private static void send(Webhook webhook) {
        if (webhook == null) return;
        client.send(webhook);
    }

    public static void stop() {
        if (client == null) return;
        client.close();
        client = null;
    }
}
