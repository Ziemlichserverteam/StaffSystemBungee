package de.staticfx.staffsystem.filemanagment;

import de.staticfx.staffsystem.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManagment {

    File file = new File(Main.getInstance().getDataFolder().getAbsolutePath(), "config.yml");
    Configuration conf;

    public static ConfigManagment INSTANCE = new ConfigManagment();

    public void loadFile() {
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(in,file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
           conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveFile();
    }

    public void saveFile() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(conf,file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getString(String path) {
        return conf.getString(path);
    }

    public String getUser() {
        return conf.getString("SQL_USER");
    }

    public String getPassword() {
        return conf.getString("SQL_PASSWORD");
    }

    public String getDataBase() {
        return conf.getString("SQL_DATABASE");
    }


}
