package me.zailer.plotcubic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import marcono1234.gson.recordadapter.RecordTypeAdapterFactory;
import me.zailer.plotcubic.PlotCubic;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;

public class ConfigManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String CONFIG_FILE = PlotCubic.class.getSimpleName() + ".json";

    @Nullable
    public Config getConfig() {
        return config;
    }

    private File file;
    private Config config;

    public ConfigManager() {
        try {
            file = Files.createDirectories(FabricLoader.getInstance().getConfigDir()).resolve(CONFIG_FILE).toFile();

            if (!file.exists()) {
                config = loadDefault();
                return;
            }

            Gson gson = new GsonBuilder().registerTypeAdapterFactory(RecordTypeAdapterFactory.DEFAULT).create();

            config = gson.fromJson(Files.readString(file.toPath()), Config.class);
        } catch (IOException e) {
            LOGGER.error("[PlotCubic] An error occurred while loading the configuration file.", e);
        }
    }

    private Config loadDefault() throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(RecordTypeAdapterFactory.DEFAULT).setPrettyPrinting().create();

        Config cfg = Config.DEFAULT;

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(gson.toJson(cfg, Config.class));
        writer.close();

        return cfg;
    }

    public void reload() throws IOException {
        if (!file.exists()) {
            config = loadDefault();
            return;
        }

        Gson gson = new Gson();
        config = gson.fromJson(Files.readString(file.toPath()), Config.class);
    }
}
