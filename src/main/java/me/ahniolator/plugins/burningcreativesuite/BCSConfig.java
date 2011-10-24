package me.ahniolator.plugins.burningcreativesuite;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class BCSConfig {

    private final BurningCreativeSuite plugin;
    private final BCSPlayerListener playerListener;
    private final BCSBlockListener blockListener;
    private final BCSConfig config;
    private final BCSEntityListener entityListener;
    private String dir;
    private File configFile;
    public YamlConfiguration yml = new YamlConfiguration();

    public BCSConfig(BurningCreativeSuite plugin, BCSPlayerListener playerListener, BCSBlockListener blockListener, BCSConfig config, String dir, BCSEntityListener entityListener) {
        this.plugin = plugin;
        this.playerListener = playerListener;
        this.config = config;
        this.blockListener = blockListener;
        this.dir = dir;
        this.entityListener = entityListener;
        this.configFile = new File(dir + "config.yml");
        System.out.println("[BurningCS] Attempting to load config file");
        load();
    }

    public void load() {
        if (!this.configFile.exists()) {
            try {
                System.out.println("[BurningCS] Could not find the config file! Making a new one.");
                new File(this.dir).mkdir();
                this.configFile.createNewFile();
                setDefaults();
                this.yml.load(configFile);
                System.out.println("[BurningCS] Config file loaded successfully!");
            } catch (Exception e) {
                System.out.println("[BurningCS] Could not read config file!");
                e.printStackTrace();
            }
        } else {
            try {
                this.configFile.createNewFile();
                this.yml.load(configFile);
                System.out.println("[BurningCS] Config file loaded successfully!");
            } catch (Exception e) {
                System.out.println("[BurningCS] Could not read config file!");
                e.printStackTrace();
            }
        }
    }

    public void setDefaults() {
        System.out.println("[BurningCS] Setting default config values");
        try {
            new File(this.dir).mkdir();
            this.configFile.delete();
            this.configFile.createNewFile();
        } catch (Exception e) {
            System.out.println("[BurningCS] Could not read config file!");
            e.printStackTrace();
        }
        this.yml.set("Enderman.Disable Pickup", true);
        this.yml.set("Creative Players.Disable Item Dropping", true);
        this.yml.set("Creative Players.Disable Item Pickup", true);
        this.yml.set("Creative Players.Placed Blocks Give No Drops.Players", true);
        this.yml.set("Creative Players.Placed Blocks Give No Drops.Explosions", true);
        this.yml.set("Creative Players.Disable Bottom-of-the-World Bedrock Break", true);
        this.yml.set("Creative Players.Attack other entities", false);
        this.yml.set("Game Mode.Separate Inventories", true);
        this.yml.set("Update.Notifications", true);
        this.yml.set("Blocks Save Interval", 15);
        try {
            this.yml.save(configFile);
        } catch (IOException ex) {
            System.out.println("[BurningCS] Could not save config file!");
            ex.printStackTrace();
        }
        System.out.println("[BurningCS] Default values set!");
    }

    public void reload() {
        try {
            this.yml.save(configFile);
            this.yml.load(configFile);
        } catch (Exception e) {
            System.out.println("[BurningCS] Could not read config file!");
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.yml.save(configFile);
        } catch (Exception e) {
            System.out.println("[BurningCS] Could not save config file!");
            e.printStackTrace();
        }
    }
}
