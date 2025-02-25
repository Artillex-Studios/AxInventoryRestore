package com.artillexstudios.axinventoryrestore.libraries;

import com.artillexstudios.axapi.libs.libby.Library;
import com.artillexstudios.axapi.libs.libby.relocation.Relocation;

public enum Libraries {

    MYSQL_CONNECTOR("com{}mysql:mysql-connector-j:8.0.33"),

    H2_JDBC("com{}h2database:h2:2.1.214"),

    POSTGRESQL("org{}postgresql:postgresql:42.5.4"),

    HIKARICP("com{}zaxxer:HikariCP:5.1.0", new Relocation("com{}zaxxer{}hikari", "com.artillexstudios.axinventoryrestore.libs.hikari"));

    private final Library library;

    public Library getLibrary() {
        return this.library;
    }

    Libraries(String lib, Relocation relocation) {
        String[] split = lib.split(":");

        library = Library.builder()
                .groupId(split[0])
                .artifactId(split[1])
                .version(split[2])
                .relocate(relocation)
                .build();
    }

    Libraries(String lib) {
        String[] split = lib.split(":");

        library = Library.builder()
                .groupId(split[0])
                .artifactId(split[1])
                .version(split[2])
                .build();
    }
}
