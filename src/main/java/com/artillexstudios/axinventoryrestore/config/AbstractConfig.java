package com.artillexstudios.axinventoryrestore.config;

import dev.dejvokep.boostedyaml.YamlDocument;

public interface AbstractConfig {
    void setup();
    YamlDocument getConfig();
}
