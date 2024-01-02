package com.artillexstudios.axinventoryrestore.libraries;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;

public enum Libraries {

    BOOSTED_YAML("dev{}dejvokep:boosted-yaml:1.3"),

    MYSQL_CONNECTOR("com{}mysql:mysql-connector-j:8.0.33"),

    MARIADB_CONNECTOR("org{}mariadb{}jdbc:mariadb-java-client:3.1.3"),

    SQLITE_JDBC("org{}xerial:sqlite-jdbc:3.42.0.0"),

    H2_JDBC("com{}h2database:h2:2.1.214"),

    POSTGRESQL("org{}postgresql:postgresql:42.5.4"),

    JDA("net{}dv8tion:JDA:5.0.0-beta.18");

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
