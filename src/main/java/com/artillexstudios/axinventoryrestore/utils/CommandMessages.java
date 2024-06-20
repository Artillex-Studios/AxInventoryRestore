package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axapi.utils.StringUtils;
import revxrsal.commands.locales.LocaleReader;

import java.util.Locale;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class CommandMessages implements LocaleReader {
    @Override
    public boolean containsKey(String s) {
        return true;
    }

    @Override
    public String get(String s) {
        String res;
        switch (s) {
            case "invalid-enum", "invalid-number", "invalid-uuid", "invalid-url", "invalid-boolean": {
                res = MESSAGES.getString("commands.invalid-value")
                        .replace("%value%", "{0}");
                break;
            }
            case "missing-argument": {
                res = MESSAGES.getString("commands.missing-argument")
                        .replace("%value%", "{0}");
                break;
            }
            case "no-permission": {
                res = MESSAGES.getString("commands.no-permission");
                break;
            }
            case "number-not-in-range": {
                res = MESSAGES.getString("commands.out-of-range")
                        .replace("%number%", "{0}")
                        .replace("%min%", "{1}")
                        .replace("%max%", "{2}");
                break;
            }
            case "must-be-player": {
                res = MESSAGES.getString("commands.player-only");
                break;
            }
            case "must-be-console": {
                res = MESSAGES.getString("commands.console-only");
                break;
            }
            case "invalid-player": {
                res = MESSAGES.getString("commands.invalid-player")
                        .replace("%player%", "{0}");
                break;
            }
            case "invalid-selector": {
                res = MESSAGES.getString("commands.invalid-selector");
                break;
            }
            default:  {
                res = MESSAGES.getString("commands.invalid-command");
                break;
            }
        }
        return StringUtils.formatToString(CONFIG.getString("prefix", "") + res);
    }

    private final Locale locale = new Locale("en", "US");

    @Override
    public Locale getLocale() {
        return locale;
    }
}
