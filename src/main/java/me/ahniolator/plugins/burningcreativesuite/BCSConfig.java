package me.ahniolator.plugins.burningcreativesuite;

import java.io.File;
import org.bukkit.util.config.Configuration;

public final class BCSConfig {

    private final BurningCreativeSuite plugin;
    private final BCSPlayerListener playerListener;
    private final BCSBlockListener blockListener;
    private final BCSConfig config;
    private final BCSEntityListener entityListener;
    private String dir;
    private File configFile;
    public Configuration yml;

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
                this.yml = new Configuration(this.configFile);
                setDefaults();
                System.out.println("[BurningCS] Config file loaded successfully!");
            } catch (Exception e) {
                System.out.println("[BurningCS] Could not read config file!");
                e.printStackTrace();
            }
        } else {
            try {
                this.yml = new Configuration(this.configFile);
                this.yml.load();
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
            this.yml = new Configuration(this.configFile);
        } catch (Exception e) {
            System.out.println("[BurningCS] Could not read config file!");
            e.printStackTrace();
        }
        this.yml.setProperty("Enderman.Disable Pickup", true);
        this.yml.setProperty("Creative Players.Disable Item Dropping", true);
        this.yml.setProperty("Creative Players.Placed Blocks Give No Drops.Players", true);
        this.yml.setProperty("Creative Players.Placed Blocks Give No Drops.Explosions", true);
        this.yml.setProperty("Creative Players.Disable Bottom-of-the-World Bedrock Break", true);
        this.yml.setProperty("Creative Players.Attack other entities", false);
        this.yml.setProperty("Game Mode.Separate Inventories", true);
        this.yml.setProperty("Update.Notifications", true);
        this.yml.setProperty("Blocks Save Interval", 15);
        this.yml.save();
        System.out.println("[BurningCS] Default values set!");
        this.yml.load();
    }

    public void reload() {
        try {
            this.yml.save();
            this.yml.load();
        } catch (Exception e) {
            System.out.println("[BurningCS] Could not read config file!");
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.yml.save();
        } catch (Exception e) {
            System.out.println("[BurningCS] Could not save config file!");
            e.printStackTrace();
        }
    }
}
