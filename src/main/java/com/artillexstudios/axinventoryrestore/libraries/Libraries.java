package com.artillexstudios.axinventoryrestore.libraries;

import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;

public enum Libraries {
    ADVENTURE_MINI_MESSAGE("net{}kyori:adventure-text-minimessage:4.14.0",
            Relocation.builder().pattern("net{}kyori").relocatedPattern("com{}artillexstudios{}axinventoryrestore{}libs{}adventure").build()
    ),

    ADVENTURE_LEGACY("net{}kyori:adventure-text-serializer-legacy:4.14.0",
            Relocation.builder().pattern("net{}kyori").relocatedPattern("com{}artillexstudios{}axinventoryrestore{}libs{}adventure").build()
    ),

    ADVENTURE_GSON("net{}kyori:adventure-text-serializer-gson:4.14.0",
            Relocation.builder().pattern("net{}kyori").relocatedPattern("com{}artillexstudios{}axinventoryrestore{}libs{}adventure").build()
    ),

    ADVENTURE_BUKKIT("net{}kyori:adventure-platform-bukkit:4.3.0",
            Relocation.builder().pattern("net{}kyori").relocatedPattern("com{}artillexstudios{}axinventoryrestore{}libs{}adventure").build()
    ),

    ADVENTURE_CORE("net{}kyori:adventure-api:4.14.0",
            Relocation.builder().pattern("net{}kyori").relocatedPattern("com{}artillexstudios{}axinventoryrestore{}libs{}adventure").build()
    ),

    BOOSTED_YAML("dev{}dejvokep:boosted-yaml:1.3"),

    HIKARICP("com{}zaxxer:HikariCP:5.0.1",
        Relocation.builder().pattern("com{}zaxxer").relocatedPattern("com{}artillexstudios{}libs{}hikaricp").build()
    ),

    MYSQL_CONNECTOR("com{}mysql:mysql-connector-j:8.0.33"),

    MARIADB_CONNECTOR("org{}mariadb{}jdbc:mariadb-java-client:3.1.3"),

    SQLITE_JDBC("org{}xerial:sqlite-jdbc:3.42.0.0"),

    H2_JDBC("com{}h2database:h2:2.1.214"),

    POSTGRESQL("org{}postgresql:postgresql:42.5.4");

    private final net.byteflux.libby.Library library;

    public net.byteflux.libby.Library getLibrary() {
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
