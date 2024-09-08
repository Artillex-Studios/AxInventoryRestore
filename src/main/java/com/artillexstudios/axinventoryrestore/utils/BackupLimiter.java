package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;

import java.util.UUID;

public class BackupLimiter {

    public static void tryLimit(UUID uuid, String reason, String reason2) {
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            int withReason = AxInventoryRestore.CONFIG.getInt("save-limits." + reason);
            if (withReason != -1) {
                int saves = AxInventoryRestore.getDB().getSaves(uuid, reason2);
                int difference = withReason - saves;
                if (difference <= 0) {
                    AxInventoryRestore.getDB().removeLastSaves(uuid, reason2, difference * (-1));
                }
            }

            int total = AxInventoryRestore.CONFIG.getInt("save-limits.total");
            if (total != -1) {
                int saves = AxInventoryRestore.getDB().getSaves(uuid, null);
                int difference = total - saves;
                if (difference <= 0) {
                    AxInventoryRestore.getDB().removeLastSaves(uuid, null, difference * (-1));
                }
            }
        });
    }
}
