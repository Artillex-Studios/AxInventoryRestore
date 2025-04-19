package com.artillexstudios.axinventoryrestore.utils;

import club.minnced.discord.webhook.send.WebhookEmbed;
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class WebhookEmbedBuilder {
    private final club.minnced.discord.webhook.send.WebhookEmbedBuilder embed;
    private final Map<String, String> replacements;

    public WebhookEmbedBuilder(@NotNull Section section) {
        this(section, new HashMap<>());
    }

    public WebhookEmbedBuilder(@NotNull Section section, Map<String, String> replacements) {
        this.replacements = replacements;
        this.embed = new club.minnced.discord.webhook.send.WebhookEmbedBuilder();

        section.getOptionalString("color").ifPresent(this::setColor);
        section.getOptionalString("description").ifPresent(this::setDescription);
        section.getOptionalString("image-url").ifPresent(this::setImageUrl);
        section.getOptionalString("thumbnail-url").ifPresent(this::setThumbnailUrl);
        section.getOptionalString("title.text").ifPresent((val) -> {
            this.setTitle(val, section.getString("title.icon"));
        });
        section.getOptionalString("author.name").ifPresent((val) -> {
            this.setAuthor(val, section.getString("author.icon-url"), section.getString("author.url"));
        });
        section.getOptionalString("footer.text").ifPresent((val) -> {
            this.setFooter(val, section.getString("footer.icon"));
        });
        section.getOptionalSection("fields").ifPresent((val) -> {
            for (String route : val.getRoutesAsStrings(false)) {
                section.getOptionalString("fields." + route + ".name").ifPresent((val2) -> {
                    this.addField(section.getBoolean("fields." + route + ".inline", false), val2, section.getString("fields." + route + ".value", ""));
                });
            }
        });
    }

    public WebhookEmbed get() {
        return embed.build();
    }

    public WebhookEmbedBuilder setColor(String color) {
        embed.setColor(Integer.parseInt(color.replace("#", ""), 16));
        return this;
    }

    public WebhookEmbedBuilder setDescription(String description) {
        embed.setDescription(replacePlaceholders(description));
        return this;
    }

    public WebhookEmbedBuilder setImageUrl(String imageUrl) {
        embed.setImageUrl(imageUrl);
        return this;
    }

    public WebhookEmbedBuilder setThumbnailUrl(String thumbnailUrl) {
        embed.setThumbnailUrl(thumbnailUrl);
        return this;
    }

    public WebhookEmbedBuilder setTitle(@NotNull String text, @Nullable String icon) {
        embed.setTitle(new WebhookEmbed.EmbedTitle(replacePlaceholders(text), icon));
        return this;
    }

    public WebhookEmbedBuilder setAuthor(@NotNull String name, @Nullable String iconUrl, @Nullable String url) {
        embed.setAuthor(new WebhookEmbed.EmbedAuthor(replacePlaceholders(name), iconUrl, url));
        return this;
    }

    public WebhookEmbedBuilder setFooter(@NotNull String text, @Nullable String icon) {
        embed.setFooter(new WebhookEmbed.EmbedFooter(replacePlaceholders(text), icon));
        return this;
    }

    public WebhookEmbedBuilder addField(boolean inline, @NotNull String name, @NotNull String value) {
        embed.addField(new WebhookEmbed.EmbedField(inline, replacePlaceholders(name), replacePlaceholders(value)));
        return this;
    }

    public WebhookEmbedBuilder setTimeSpan(@Nullable TemporalAccessor timestamp) {
        embed.setTimestamp(timestamp);
        return this;
    }

    private String replacePlaceholders(@NotNull String string) {
        AtomicReference<String> message = new AtomicReference<>(string);
        replacements.forEach((key, value) -> message.set(message.get().replace(key, value)));
        return message.get();
    }
}
