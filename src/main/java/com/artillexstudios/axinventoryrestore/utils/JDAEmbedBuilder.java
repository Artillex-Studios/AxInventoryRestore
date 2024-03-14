package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class JDAEmbedBuilder {
    private final net.dv8tion.jda.api.EmbedBuilder embed;
    private final Map<String, String> replacements;

    public JDAEmbedBuilder(Section section, Map<String, String> replacements) {
        this.embed = new net.dv8tion.jda.api.EmbedBuilder();
        this.replacements = replacements;

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

    public MessageEmbed get() {
        return embed.build();
    }

    public JDAEmbedBuilder setColor(String color) {
        embed.setColor(Integer.parseInt(color.replace("#", ""), 16));
        return this;
    }

    public JDAEmbedBuilder setDescription(String description) {
        embed.setDescription(replacePlaceholders(description));
        return this;
    }

    public JDAEmbedBuilder setImageUrl(String imageUrl) {
        embed.setImage(imageUrl);
        return this;
    }

    public JDAEmbedBuilder setThumbnailUrl(String thumbnailUrl) {
        embed.setThumbnail(thumbnailUrl);
        return this;
    }

    public JDAEmbedBuilder setTitle(@NotNull String text, @Nullable String icon) {
        embed.setTitle(replacePlaceholders(text), icon);
        return this;
    }

    public JDAEmbedBuilder setAuthor(@NotNull String name, @Nullable String iconUrl, @Nullable String url) {
        embed.setAuthor(replacePlaceholders(name), url, iconUrl);
        return this;
    }

    public JDAEmbedBuilder setFooter(@NotNull String text, @Nullable String icon) {
        embed.setFooter(replacePlaceholders(text), icon);
        return this;
    }

    public JDAEmbedBuilder addField(boolean inline, @NotNull String name, @NotNull String value) {
        embed.addField(replacePlaceholders(name), replacePlaceholders(value), inline);
        return this;
    }

    public JDAEmbedBuilder setTimestamp(@Nullable TemporalAccessor timestamp) {
        embed.setTimestamp(timestamp);
        return this;
    }

    private String replacePlaceholders(@NotNull String str) {
        final AtomicReference<String> toFormat = new AtomicReference<>(str);
        replacements.forEach((pattern, replacement) -> {
            toFormat.set(toFormat.get().replace(pattern, replacement));
        });

        return toFormat.get();
    }
}
