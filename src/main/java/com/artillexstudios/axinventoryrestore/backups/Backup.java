package com.artillexstudios.axinventoryrestore.backups;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Backup {
    private final ArrayList<BackupData> backupDataList;
    private final HashMap<String, List<BackupData>> deathsPerTypes = new HashMap<>();

    public Backup(ArrayList<BackupData> backupDataList) {
        this.backupDataList = backupDataList;

        calculate();
    }

    private void calculate() {
        for (BackupData backupData : backupDataList) {
            if (deathsPerTypes.containsKey(backupData.getReason())) {
                final List<BackupData> newAr = deathsPerTypes.get(backupData.getReason());
                newAr.add(backupData);
                deathsPerTypes.put(backupData.getReason(), newAr);
            } else {
                deathsPerTypes.put(backupData.getReason(), new ArrayList<>(List.of(backupData)));
            }
        }
    }

    public ArrayList<BackupData> getBackupDataList() {
        return backupDataList;
    }

    public HashMap<String, List<BackupData>> getDeathsPerTypes() {
        return deathsPerTypes;
    }

    public List<BackupData> getDeathsByReason(@NotNull String saveReason) {
        if (saveReason.equals("ALL")) return backupDataList;
        return deathsPerTypes.get(saveReason);
    }
}
