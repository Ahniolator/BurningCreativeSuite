package me.ahniolator.plugins.burningcreativesuite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BCSUpdater implements Runnable {

    private BurningCreativeSuite plugin = null;
    private Player player = null;
    private boolean response;
    private CommandSender cs = null;
    private static String changes, currentVerUrl = "http://ahniolator.aisites.com/BCSversion.txt";
    private static boolean tellUpdate, isFinished = false;
    private static double scriptVersion;
    private static BufferedReader r;

    public BCSUpdater(BurningCreativeSuite plugin, Player player, boolean response) {
        this.plugin = plugin;
        this.player = player;
        this.response = response;
    }

    public BCSUpdater(BurningCreativeSuite plugin, CommandSender cs) {
        this.plugin = plugin;
        this.cs = cs;
    }

    @Override
    public void run() {
        if (!(this.player == null)) {
            checkForUpdates(this.plugin, this.player, this.response);
            return;
        } else {
            checkForUpdates(this.plugin, this.cs);
        }
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

    public static double getCurrentVersion(final Player player, String site) {
        try {
            //timeout timer
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (BCSUpdater.isFinished) {
                        //System.out.println("[MagnaIpsum] Success! :D");
                        BCSUpdater.isFinished = false;
                        return;
                    }
                    try {
                        r.close();
                        throw new CouldNotConnectException();
                    } catch (CouldNotConnectException ex) {
                        player.sendMessage(ex.getMessage());
                    } catch (IOException ex) {
                        System.out.println("Something just went wrong... REALLY wrong. And it's probably my fault. This error should never show if the URL is correct");
                        ex.printStackTrace();
                    }
                }
            }, 10 * 1000); //delay in seconds delay in seconds * 1000 = seconds
            r = new BufferedReader(new InputStreamReader(new URL(site).openStream()));
            String[] string = r.readLine().split(";");
            changes = string[1];
            double d = Double.parseDouble(string[0]);
            r.close();
            BCSUpdater.isFinished = true;
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

    public static double getCurrentVersion(final CommandSender player, String site) {
        try {
            //timeout timer
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (BCSUpdater.isFinished) {
                        //System.out.println("[MagnaIpsum] Success! :D");
                        BCSUpdater.isFinished = false;
                        return;
                    }
                    try {
                        r.close();
                        throw new CouldNotConnectException();
                    } catch (CouldNotConnectException ex) {
                        player.sendMessage(ex.getMessage());
                    } catch (IOException ex) {
                        System.out.println("Something just went wrong... REALLY wrong. And it's probably my fault. This error should never show if the URL is correct");
                        ex.printStackTrace();
                    }
                }
            }, 10 * 1000); //delay in seconds delay in seconds * 1000 = seconds
            r = new BufferedReader(new InputStreamReader(new URL(site).openStream()));
            String[] string = r.readLine().split(";");
            changes = string[1];
            double d = Double.parseDouble(string[0]);
            r.close();
            BCSUpdater.isFinished = true;
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
