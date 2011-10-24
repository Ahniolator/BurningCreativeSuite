package me.ahniolator.plugins.burningcreativesuite;

import java.io.File;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.diddiz.LogBlock.LogBlock;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BurningCreativeSuite extends JavaPlugin {

    public BCSPlayerListener playerListener;
    public BCSBlockListener blockListener;
    public BCSConfig config;
    public BCSEntityListener entityListener;
    public BCSInventoryManager invManager = new BCSInventoryManager();
    public boolean isTimeFrozen = false;
    public World world;
    public long startTime;
    public WorldGuardPlugin worldGuard;
    public LogBlock logBlock;
    public boolean isFinished = false;
    public URL url;
    public BufferedReader in;
    private String dir;
    private String dataDir;
    private static boolean tellUpdate;

    public void onDisable() {
        this.blockListener.saveBlockData();
        System.out.println("[BurningCS] v" + this.getDescription().getVersion() + " is now disabled!");
    }

    public void onEnable() {
        try {
            url = new URL("http://ahniolator.aisites.com/visitors/visit.php?v=" + this.getDescription().getVersion());
        } catch (MalformedURLException ex) {
        }
        dir = this.getDataFolder() + File.separator;
        dataDir = dir + "data" + File.separator;
        this.config = new BCSConfig(this, this.playerListener, this.blockListener, this.config, this.dir, this.entityListener);
        tellUpdate = this.config.yml.getBoolean("Update.Notifications", true);
        this.blockListener = new BCSBlockListener(this, this.playerListener, this.blockListener, this.config, this.entityListener, this.dataDir, this.invManager);
        this.playerListener = new BCSPlayerListener(this, this.playerListener, this.blockListener, this.config, this.entityListener, this.dataDir, this.invManager, tellUpdate);
        this.entityListener = new BCSEntityListener(this, this.playerListener, this.blockListener, this.config, this.entityListener, this.dataDir, this.invManager);
        PluginManager pluginManager = getServer().getPluginManager();
        Plugin plugin = pluginManager.getPlugin("WorldGuard");
        if (plugin instanceof WorldGuardPlugin) {
            worldGuard = (WorldGuardPlugin) plugin;
            System.out.println("[BurningCS] Hooking into WorldGuard.");
        }
        plugin = pluginManager.getPlugin("LogBlock");
        if (plugin instanceof LogBlock) {
            logBlock = (LogBlock) plugin;
            System.out.println("[BurningCS] Hooking into LogBlock.");
        }
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_GAME_MODE_CHANGE, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT_ENTITY, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENDERMAN_PICKUP, entityListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
        System.out.println("[BurningCS] v" + this.getDescription().getVersion() + " is now enabled!");
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {

            public void run() {
                try {
                    //timeout timer
                    Timer timer = new Timer(true);
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            if (isFinished) {
                                //System.out.println("[MagnaIpsum] Success! :D");
                                isFinished = false;
                                return;
                            }
                            try {
                                //System.out.println("Closing connection.");
                                in.close();
                            } catch (IOException ex) {
                                System.out.println("Something just went wrong... REALLY wrong. And it's probably my fault. This error should never show if the URL is correct");
                                ex.printStackTrace();
                            }
                        }
                    }, 5 * 1000); //delay in seconds delay in seconds * 1000 = seconds
                    //System.out.println("Opening Connection.");
                    in = new BufferedReader(
                            new InputStreamReader(
                            url.openStream()));
                    in.close();
                    isFinished = true;
                } catch (IOException ex) {
                }
            }
        }, 0, 1000 * 60 * 7);
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
                    if (cs instanceof Player && !player.hasPermission("bcs.admin")) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You do not have the correct permissions!");
                        return true;
                    }
                    for (Player playerTarg : this.getServer().getOnlinePlayers()) {
                        if (playerTarg.getName().equalsIgnoreCase(name)) {
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
                    return true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (!(cs instanceof Player)) {
                        cs.sendMessage(ChatColor.RED + "[BurningCS] You have not entered the correct command");
                        cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You must specify a player!");
                        return true;
                    }
                    if (!player.hasPermission("bcs.admin")) {
                        if (!player.hasPermission("bcs.commands.toggle")) {
                            cs.sendMessage(ChatColor.RED + "[BurningCS] You do not have access to this command");
                            cs.sendMessage(ChatColor.RED + "[BurningCS] [REASON] You do not have the correct permissions!");
                            return true;
                        }
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
                        this.config.yml.set("Enderman.Disable Pickup", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Enderman block placement/pickup is now " + ChatColor.RED + "DISABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.set("Enderman.Disable Pickup", false);
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
                        this.config.yml.set("Creative Players.Disable Item Dropping", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Creative player item dropping is now " + ChatColor.RED + "DISABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.set("Creative Players.Disable Item Dropping", false);
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
                        this.config.yml.set("Creative Players.Placed Blocks Give No Drops.Players", true);
                        this.config.yml.set("Creative Players.Placed Blocks Give No Drops.Explosions", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Creative blocks item dropping is now " + ChatColor.RED + "DISABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.set("Creative Players.Placed Blocks Give No Drops.Players", false);
                        this.config.yml.set("Creative Players.Placed Blocks Give No Drops.Explosions", false);
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
                        this.config.yml.set("Creative Players.Disable Bottom-of-the-World Bedrock Break", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Bottom-of-the-World bedrock breaking is now " + ChatColor.RED + "DISABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("enable")) {
                        this.config.yml.set("Creative Players.Disable Bottom-of-the-World Bedrock Break", false);
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
                        this.config.yml.set("Game Mode.Separate Inventories", true);
                        this.config.reload();
                        cs.sendMessage("[BurningCS] Separate inventories are now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.set("Game Mode.Separate Inventories", false);
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
                this.config.reload();
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
                        this.config.yml.set("Update.Notifications", true);
                        this.config.reload();
                        tellUpdate = this.config.yml.getBoolean("Update.Notifications", true);
                        cs.sendMessage("[BurningCS] Update notifications are now" + ChatColor.GREEN + " ENABLED");
                        return true;
                    } else if (arg.equalsIgnoreCase("disable")) {
                        this.config.yml.set("Update.Notifications", false);
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
                        this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new BCSUpdater(this, player, true));
                        return true;
                    } else {
                        this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new BCSUpdater(this, cs));
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
}
