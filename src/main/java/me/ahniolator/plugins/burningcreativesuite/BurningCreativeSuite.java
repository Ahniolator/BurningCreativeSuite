package me.ahniolator.plugins.burningcreativesuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import me.ahniolator.plugins.burningcreativesuite.commands.BCSGiveExecutor;
import me.ahniolator.plugins.burningcreativesuite.wand.BCSWandExecutor;
import me.ahniolator.plugins.burningcreativesuite.wand.BCSWandPlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class BurningCreativeSuite extends JavaPlugin {

//      give:
//    description: Gives a player an item
//    usage: /<command> [player] <item>
//  bcswand:
//    description: Handles all "wand" functions
//    usage: /<command> <help>
    public BCSPlayerListener playerListener;
    public BCSBlockListener blockListener;
    public BCSConfig config;
    public BCSEntityListener entityListener;
    public BCSInventoryManager invManager = new BCSInventoryManager();
    public BCSGiveExecutor bcsge;
    public BCSWandExecutor bcswe;
    public BCSWandPlayerListener bcswpl;
    public boolean isTimeFrozen = false;
    public World world;
    public long startTime;
    private String dir;
    private String dataDir;
    private Configuration yml;
    private static double scriptVersion;
    private static boolean tellUpdate, newimplemented = false;
    private static String changes, currentVerUrl = "http://ahniolator.aisites.com/BCSversion.txt";

    public void onDisable() {
        this.blockListener.saveBlockData();
        System.out.println("[BurningCS] v" + this.getDescription().getVersion() + " is now disabled!");
    }

    public void onEnable() {
        dir = this.getDataFolder() + File.separator;
        dataDir = dir + "data" + File.separator;
        scriptVersion = Double.valueOf(Double.parseDouble(this.getDescription().getVersion())).doubleValue();
        this.config = new BCSConfig(this, this.playerListener, this.blockListener, this.config, this.dir, this.entityListener);
        tellUpdate = this.config.yml.getBoolean("Update.Notifications", true);
        this.blockListener = new BCSBlockListener(this, this.playerListener, this.blockListener, this.config, this.entityListener, this.dataDir, this.invManager);
        this.playerListener = new BCSPlayerListener(this, this.playerListener, this.blockListener, this.config, this.entityListener, this.dataDir, this.invManager, tellUpdate);
        this.entityListener = new BCSEntityListener(this, this.playerListener, this.blockListener, this.config, this.entityListener, this.dataDir, this.invManager);
        if (newimplemented) {
            this.bcsge = new BCSGiveExecutor(this, this.config);
            this.bcswe = new BCSWandExecutor(this, this.config);
            this.bcswpl = new BCSWandPlayerListener(this, this.config);
            this.getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, bcswpl, Priority.Low, this);
        }
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_GAME_MODE_CHANGE, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENDERMAN_PICKUP, entityListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
        System.out.println("[BurningCS] v" + this.getDescription().getVersion() + " is now enabled!");
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        Player player = null;

        if (cmnd.getName().equalsIgnoreCase("bcs")) {
            String arg = "";
            String name = "";

            if ((cs instanceof Player)) {
                player = (Player) cs;
            }

            try {
                arg = args[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }

            if (arg.equalsIgnoreCase("clearinv")) {
                try {
                    name = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You do not have the correct permissions!");
                        return true;
                    }
                    for (Player playerTarg : this.getServer().getOnlinePlayers()) {
                        if (playerTarg.getDisplayName().equalsIgnoreCase(name)) {
                            playerListener.clearInv(playerTarg, cs);
                        } else {
                            continue;
                        }
                        return true;
                    }
                    cs.sendMessage(ChatColor.RED + "[BurningCS] Command failed to excecute!");
                    cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] There is no online player with that name!");
                    return true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (!(cs instanceof Player)) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command.");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You are not a player! Try /bcs clearinv <playername>");
                        return true;
                    }
                    if (!player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You do not have the correct permissions!");
                        return true;
                    }
                    playerListener.clearInv(player);
                    return true;
                }
            }
            if (arg.equalsIgnoreCase("toggle")) {
                try {
                    name = args[1];
                    if (cs instanceof Player && !player.hasPermission("bcs.admin") && !player.hasPermission("bcs.commands.toggle")) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You do not have the correct permissions!");
                        return true;
                    }
                    for (Player playerTarg : this.getServer().getOnlinePlayers()) {
                        if (playerTarg.getDisplayName().equalsIgnoreCase(name)) {
                            if (playerTarg.getGameMode() == GameMode.CREATIVE) {
                                playerTarg.sendMessage("[BurningCS] You are now in SURVIVAL mode");
                                cs.sendMessage("[BurningCS] Player " + name + " is now in SURVIVAL mode");
                                playerTarg.setGameMode(GameMode.SURVIVAL);
                                return true;
                            } else if (playerTarg.getGameMode() == GameMode.SURVIVAL) {
                                playerTarg.setGameMode(GameMode.CREATIVE);
                                playerTarg.sendMessage("[BurningCS] You are now in CREATIVE mode");
                                cs.sendMessage("[BurningCS] Player " + name + " is now in CREATIVE mode");
                                return true;
                            }
                        } else {
                            continue;
                        }
                        cs.sendMessage(ChatColor.RED + "[BurningCS] Command failed to excecute!");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] There is no online player with that name!");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (!(cs instanceof Player)) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You have not entered the correct command");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You must specify a player!");
                        return true;
                    }
                    if (!player.hasPermission("bcs.admin") && !player.hasPermission("bcs.bypass.inventory")) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You do not have the correct permissions!");
                        return true;
                    }
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        player.setGameMode(GameMode.SURVIVAL);
                        return true;
                    } else if (player.getGameMode() == GameMode.SURVIVAL) {
                        player.setGameMode(GameMode.CREATIVE);
                        return true;
                    }
                }
            }
            if (arg.equalsIgnoreCase("set")) {
                try {
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    arg = args[1];
                    if (arg.equalsIgnoreCase("default")) {
                        this.config.setDefaults();
                        cs.sendMessage("[BurningCS] Config defaults applied");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    return false;
                }
            }
            if (arg.equalsIgnoreCase("enderman") || arg.equalsIgnoreCase("endermen")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty("Enderman.Disable Pickup", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Enderman block placement/pickup is now " + ChatColor.RED + "DISABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty("Enderman.Disable Pickup", false);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Enderman block placement/pickup is now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    boolean burningEnabled = this.config.yml.getBoolean("Enderman.Disable Pickup", true);
                    if (!burningEnabled) {
                        cs.sendMessage("[BurningCS] Enderman block placement/pickup is" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (burningEnabled) {
                        cs.sendMessage("[BurningCS] Enderman block placement/pickup" + ChatColor.RED + " DISABLED");
                        return true;
                    }
                }
            }
            if (arg.equalsIgnoreCase("drops")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty("Creative Players.Disable Item Dropping", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Creative player item dropping is now " + ChatColor.RED + "DISABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty("Creative Players.Disable Item Dropping", false);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Creative player item dropping is now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    boolean burningEnabled = this.config.yml.getBoolean("Creative Players.Disable Item Dropping", true);
                    if (!burningEnabled) {
                        cs.sendMessage("[BurningCS] Creative player item dropping is" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (burningEnabled) {
                        cs.sendMessage("[BurningCS] Creative player item dropping is" + ChatColor.RED + " DISABLED");
                        return true;
                    }
                }
            }
            if (arg.equalsIgnoreCase("blocks")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty("Creative Players.Placed Blocks Give No Drops.Players", true);
                        this.config.yml.setProperty("Creative Players.Placed Blocks Give No Drops.Explosions", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Creative blocks item dropping is now " + ChatColor.RED + "DISABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty("Creative Players.Placed Blocks Give No Drops.Players", false);
                        this.config.yml.setProperty("Creative Players.Placed Blocks Give No Drops.Explosions", false);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Creative blocks item dropping is now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    boolean burningEnabled = this.config.yml.getBoolean("Creative Players.Placed Blocks Give No Drops.Players", true);
                    if (!burningEnabled) {
                        cs.sendMessage("[BurningCS] Creative blocks item dropping is" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (burningEnabled) {
                        cs.sendMessage("[BurningCS] Creative blocks item dropping is" + ChatColor.RED + " DISABLED");
                        return true;
                    }
                }
            }
            if (arg.equalsIgnoreCase("bedrock")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty("Creative Players.Disable Bottom-of-the-World Bedrock Break", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Bottom-of-the-World bedrock breaking is now " + ChatColor.RED + "DISABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty("Creative Players.Disable Bottom-of-the-World Bedrock Break", false);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Bottom-of-the-World bedrock breaking is now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    boolean burningEnabled = this.config.yml.getBoolean("Creative Players.Disable Bottom-of-the-World Bedrock Break", true);
                    if (!burningEnabled) {
                        cs.sendMessage("[BurningCS] Bottom-of-the-World bedrock breaking is" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (burningEnabled) {
                        cs.sendMessage("[BurningCS] Bottom-of-the-World bedrock breaking is" + ChatColor.RED + " DISABLED");
                        return true;
                    }
                }
            }
            if (arg.equalsIgnoreCase("inventory") || arg.equalsIgnoreCase("inventories")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty("Game Mode.Separate Inventories", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Separate inventories are now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty("Game Mode.Separate Inventories", false);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Separate inventories are now " + ChatColor.RED + "DISABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    boolean burningEnabled = this.config.yml.getBoolean("Game Mode.Separate Inventories", true);
                    if (burningEnabled) {
                        cs.sendMessage("[BurningCS] Separate inventories are" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (!burningEnabled) {
                        cs.sendMessage("[BurningCS] Separate inventories are" + ChatColor.RED + " DISABLED");
                        return true;
                    }
                }
            }
            if (arg.equalsIgnoreCase("reload")) {
                if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                    cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                    return true;
                }
                cs.sendMessage("[BurningCS] Reloading Config");
                this.config.yml.load();
                cs.sendMessage("[BurningCS] Config Reloaded");
                return true;
            }
            if ((arg.equalsIgnoreCase("help"))) {
                cs.sendMessage("************  [BurningCS] Help List  ***************");
                cs.sendMessage("/bcs help: Displays this help dialog");
                cs.sendMessage("/bcs inventory [enable/disable]: Separate inventories");
                cs.sendMessage("/bcs bedrock [enable/disable]: Bottom bedrock breaking");
                cs.sendMessage("/bcs blocks [enable/disable]: Creative block drops");
                cs.sendMessage("/bcs drops [enable/disable]: Creative player drops");
                cs.sendMessage("/bcs endermen [enable/disable]: Enderman block pickup");
                cs.sendMessage("/bcs toggle: Changes game mode");
                cs.sendMessage("/bcs set default: Sets default config values");
                cs.sendMessage("/bcs clearinv: Clears your inventory");
                cs.sendMessage("/bcs time: Toggles time freezing");
                cs.sendMessage("/bcs update [enable/disable]: Update notifications");
                return true;
            }
            if (arg.equalsIgnoreCase("update") || arg.equalsIgnoreCase("notification") || arg.equalsIgnoreCase("notifications")) {
                try {
                    arg = args[1];
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.setProperty("Update.Notifications", true);
                        this.config.reload();
                        tellUpdate = this.config.yml.getBoolean("Update.Notifications", true);
                        cs.sendMessage("[BurningCS] Update notifications are now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.setProperty("Update.Notifications", false);
                        this.config.reload();
                        tellUpdate = this.config.yml.getBoolean("Update.Notifications", true);
                        cs.sendMessage("[BurningCS] Update notifications are now " + ChatColor.RED + "DISABLED");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    if ((cs instanceof Player) && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "You do not have access to this command");
                        return true;
                    }
                    if ((cs instanceof Player)) {
                        checkForUpdates(this, player, true);
                        return true;
                    } else {
                        checkForUpdates(this, cs);
                        return true;
                    }
                }
            }
            if (arg.equalsIgnoreCase("time")) {
                if (cs instanceof Player && !player.hasPermission("bcs.admin")) {
                    cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command");
                    cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You do not have the correct permissions!");
                    return true;
                }
                try {
                    arg = args[1];
                    return false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (!(cs instanceof Player)) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You are not a player!");
                        return true;
                    }
                    world = player.getWorld();
                    startTime = world.getTime();
                    if (!isTimeFrozen) {
                        cs.sendMessage(ChatColor.WHITE + "[BurningCS] Time has been" + ChatColor.RED + " STOPPED" + ChatColor.WHITE + "!");
                        isTimeFrozen = true;
                        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

                            public void run() {
                                if (isTimeFrozen) {
                                    world.setTime(startTime);
                                    return;
                                } else {
                                    return;
                                }
                            }
                        }, 0, 20 * 4);

                    } else {
                        cs.sendMessage(ChatColor.WHITE + "[BurningCS] Time has been" + ChatColor.GREEN + " RESUMED" + ChatColor.WHITE + "!");
                        isTimeFrozen = false;
                        startTime = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            if (arg.equalsIgnoreCase("devmode")) {
                if (this.playerListener.devMode) {
                    this.playerListener.devMode = false;
                    this.blockListener.devMode = false;
                    cs.sendMessage("[BurningCS] Devmode disabled!");
                } else {
                    this.playerListener.devMode = true;
                    this.blockListener.devMode = true;
                    cs.sendMessage("[BurningCS] Devmode enabled!");
                }
                return true;
            }
        }
        return false;
    }

    public static void checkForUpdates(BurningCreativeSuite plugin, Player player, boolean response) {
        scriptVersion = Double.valueOf(Double.parseDouble(plugin.getDescription().getVersion())).doubleValue();
        try {
            double currver = getCurrentVersion(player, currentVerUrl);
            if (currver > scriptVersion) {
                player.sendMessage("[BurningCS] There has been an update!");
                player.sendMessage("[BurningCS] Your current version is " + scriptVersion + ".");
                player.sendMessage("[BurningCS] The newest version is " + currver);
                player.sendMessage("[BurningCS] ChangeLog: " + changes);
                player.sendMessage("[BurningCS] Please visit the BukkitDev page to update!");
            } else {
                if (response) {
                    player.sendMessage("[BurningCS] is up to date!");
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getCurrentVersion(Player player, String site) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new URL(site).openStream()));
            String[] string = r.readLine().split(";");
            changes = string[1];
            double d = Double.parseDouble(string[0]);
            r.close();
            return d;
        } catch (Exception e) {
            player.sendMessage("[BurningCS] Error checking for latest version.");
            e.printStackTrace();
        }
        return scriptVersion;
    }

    public static void checkForUpdates(BurningCreativeSuite plugin, CommandSender player) {
        scriptVersion = Double.valueOf(Double.parseDouble(plugin.getDescription().getVersion())).doubleValue();
        try {
            double currver = getCurrentVersion(player, currentVerUrl);
            if (currver > scriptVersion) {
                player.sendMessage("[BurningCS] There has been an update!");
                player.sendMessage("[BurningCS] Your current version is " + scriptVersion + ".");
                player.sendMessage("[BurningCS] The newest version is " + currver);
                player.sendMessage("[BurningCS] ChangeLog: " + changes);
                player.sendMessage("[BurningCS] Please visit the BukkitDev page to update!");
            } else {
                player.sendMessage("[BurningCS] is up to date!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getCurrentVersion(CommandSender player, String site) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new URL(site).openStream()));
            String[] string = r.readLine().split(";");
            changes = string[1];
            double d = Double.parseDouble(string[0]);
            r.close();
            return d;
        } catch (SocketTimeoutException e) {
            player.sendMessage("[BurningCS] Could not connect to update server.");
        } catch (Exception e) {
            player.sendMessage("[BurningCS] Error checking for latest version.");
            e.printStackTrace();
        }
        return scriptVersion;
    }
}
