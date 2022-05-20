package me.zailer.plotcubic.config;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import marcono1234.gson.recordadapter.RecordTypeAdapterFactory;
import me.zailer.plotcubic.PlotCubic;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;

public class ConfigManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String CONFIG_FILE = PlotCubic.class.getSimpleName() + ".json";

    public Config getConfig() {
        return this.config;
    }

    private File file;
    private Config config;

    public ConfigManager() {
        try {
            this.file = Files.createDirectories(FabricLoader.getInstance().getConfigDir()).resolve(CONFIG_FILE).toFile();

            if (!this.file.exists()) {
                this.config = loadDefault();
                return;
            }

            try {
                this.config = this.getGson().fromJson(Files.readString(this.file.toPath()), Config.class);
            } catch (JsonParseException e) {
                this.config = this.updateJson();
            }
        } catch (IOException e) {
            LOGGER.error("[PlotCubic] An error occurred while loading the configuration file.", e);
        }
    }

    private Config loadDefault() throws IOException {
        Gson gson = this.getGson();

        Config cfg = Config.DEFAULT;

        BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
        writer.write(gson.toJson(cfg, Config.class));
        writer.close();

        return cfg;
    }

    public void reload() throws IOException {
        if (!this.file.exists()) {
            this.config = loadDefault();
            return;
        }
        this.config = this.getGson().fromJson(Files.readString(this.file.toPath()), Config.class);
    }

    private Config updateJson() throws IOException {
        Gson gson = this.getGson();
        JsonObject configJson = JsonParser.parseReader(new FileReader(file.getAbsolutePath())).getAsJsonObject();
        Config cfg = Config.DEFAULT;
        JsonObject defaultValues = gson.toJsonTree(cfg, Config.class).getAsJsonObject();

        for (var key : defaultValues.keySet()) {
            if (!configJson.has(key))
                configJson.add(key, defaultValues.get(key));
        }

        Config updatedConfig = gson.fromJson(configJson, Config.class);
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
        writer.write(gson.toJson(updatedConfig, Config.class));
        writer.close();

        return updatedConfig;
    }

    private Gson getGson() {
        return new GsonBuilder().registerTypeAdapterFactory(RecordTypeAdapterFactory.DEFAULT).setPrettyPrinting().create();
    }
}
