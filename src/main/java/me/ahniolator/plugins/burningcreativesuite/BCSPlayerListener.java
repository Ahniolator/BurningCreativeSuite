package me.ahniolator.plugins.burningcreativesuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BCSPlayerListener extends PlayerListener {

    private final BurningCreativeSuite plugin;
    private final BCSPlayerListener playerListener;
    private final BCSBlockListener blockListener;
    private final BCSConfig config;
    private final BCSEntityListener entityListener;
    private final BCSInventoryManager invManager;
    private final ArrayList<Material> torchMats = new ArrayList();
    public boolean isMultiInvOn = false, hasCheckedMulti = false, devMode = false;
    public ItemStack[] currentInv = new ItemStack[36], newInv = new ItemStack[36], currentArmor = new ItemStack[4], newArmor = new ItemStack[4];
    public String[] playerInv = new String[36], playerArmor = new String[4];
    public File creativeInv, creativeArmor, survivalInv, survivalArmor;
    public String dataDir, playerDir, playerName;
    private boolean tellUpdate;

    public BCSPlayerListener(BurningCreativeSuite plugin, BCSPlayerListener playerListener, BCSBlockListener blockListener, BCSConfig config, BCSEntityListener entityListener, String dir, BCSInventoryManager invManager, boolean update) {
        this.plugin = plugin;
        this.playerListener = playerListener;
        this.config = config;
        this.blockListener = blockListener;
        this.entityListener = entityListener;
        this.dataDir = dir + "playerData" + File.separator;
        this.invManager = invManager;
        this.tellUpdate = update;
        this.torchMats.add(Material.TORCH);
        this.torchMats.add(Material.REDSTONE_TORCH_OFF);
        this.torchMats.add(Material.REDSTONE_TORCH_ON);
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        tellUpdate = this.config.yml.getBoolean("Update.Notifications", true);
        Player player = event.getPlayer();
        if ((!player.isOp() && !player.hasPermission("bcs.admin")) || !tellUpdate) {
            return;
        }
        BurningCreativeSuite.checkForUpdates(this.plugin, player, false);
        return;
    }

    @Override
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        try {
            if (!this.config.yml.getBoolean("Game Mode.Separate Inventories", true)) {
                return;
            }
            if (!hasCheckedMulti) {
                if (plugin.getServer().getPluginManager().isPluginEnabled("MultiInv")) {
                    System.out.println("[BurningCS] MultiInv plugin found!");
                    this.isMultiInvOn = true;
                }
                hasCheckedMulti = true;
            }
            Player player = event.getPlayer();
            if (player.hasPermission("bcs.bypass.inventory")) {
                return;
            }
            playerName = player.getName();
            playerDir = dataDir + playerName + File.separator;

            currentInv = player.getInventory().getContents();
            if (currentInv == null) {
                player.sendMessage("Inventory is null!");
                return;
            }
            for (int x = 0; x < currentInv.length; ++x) {
                try {
                    if (currentInv[x] == null) {
                        this.playerInv[x] = "!,!,!,!";
                        continue;
                    }
                    this.playerInv[x] = this.invManager.toString(currentInv, x);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[BurningCS] Something went wrong! Check the console!");
                    e.printStackTrace();
                    return;
                }
            }
            creativeInv = new File(playerDir + "creative.inventory");
            survivalInv = new File(playerDir + "survival.inventory");
            if (this.invManager == null) {
                System.out.println("invManager is null");
                player.sendMessage("invManager is null");
                return;
            }
            if (event.getNewGameMode().equals(GameMode.SURVIVAL)) {
                if (!creativeInv.exists()) {
                    this.invManager.createNewFile(this.creativeInv, this.playerDir);
                }
                try {
                    if (this.playerInv == null) {
                        player.sendMessage("inventory is null");
                    }
                    if (this.creativeInv == null) {
                        player.sendMessage("inventory file is null");
                    }
                    this.invManager.saveString(playerInv, creativeInv, playerDir);
                } catch (Exception e) {
                    player.sendMessage("saving file failed!");
                    e.printStackTrace();
                }
            } else {
                if (!survivalInv.exists()) {
                    this.invManager.createNewFile(this.survivalInv, this.playerDir);
                }
                this.invManager.saveString(playerInv, survivalInv, playerDir);
            }

            currentArmor = player.getInventory().getArmorContents();
            if (currentArmor == null) {
                player.sendMessage("currentArmor is null!");
            }
            for (int x = 0; x < currentArmor.length; ++x) {
                try {
                    this.playerArmor[x] = invManager.toString(currentArmor, x);
                } catch (NullPointerException e) {
                    this.playerArmor[x] = "!,!,!,!";
                    continue;
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "[BurningCS] Something went wrong! Check the console!");
                    e.printStackTrace();
                    return;
                }
            }
            creativeArmor = new File(playerDir + "creative.armor");
            survivalArmor = new File(playerDir + "survival.armor");
            if (event.getNewGameMode().equals(GameMode.SURVIVAL)) {
                if (!creativeArmor.exists()) {
                    this.invManager.createNewFile(this.creativeArmor, this.playerDir);
                }
                try {
                    this.invManager.saveString(playerArmor, creativeArmor, playerDir);
                } catch (Exception e) {
                    player.sendMessage("saving file failed!");
                    e.printStackTrace();
                }
            } else {
                if (!survivalArmor.exists()) {
                    this.invManager.createNewFile(this.survivalArmor, this.playerDir);
                }
                this.invManager.saveString(playerArmor, survivalArmor, playerDir);
            }
            if (this.isMultiInvOn) {
                return;
            }
            this.loadInventory(player, event);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!this.config.yml.getBoolean("Creative Players.Disable Item Dropping", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission("bcm.bypass.creativedrop")) {
            return;
        }
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            event.getItemDrop().remove();
        }
        return;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (devMode && player.getItemInHand().getType().equals(Material.STICK) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            player.sendMessage("[BurningCS] This block's data value is: " + event.getClickedBlock().getData());
            player.sendMessage("[BurningCS] This block's material is: " + event.getClickedBlock().getType());
            Block block = event.getClickedBlock();
            blockListener.y = block.getLocation().getBlockY();
            blockListener.x = block.getLocation().getBlockX();
            blockListener.z = block.getLocation().getBlockZ();
            if (BCSBlockListener.blockX.contains(blockListener.x)
                    && BCSBlockListener.blockY.contains(blockListener.y)
                    && BCSBlockListener.blockZ.contains(blockListener.z)) {
                player.sendMessage("[BurningCS] Block is also a creative block!");
            }

        }
        if (player.getGameMode() == GameMode.SURVIVAL || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (!this.config.yml.getBoolean("Game Mode.Separate Inventories", true)) {
            return;
        }
        if (player.hasPermission("bcm.bypass.creativedrop")) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.FURNACE || block.getType() == Material.DISPENSER || block.getType() == Material.WORKBENCH) {
            event.setCancelled(true);
            return;
        }
        return;
    }

    public void clearInv(Player player) {
        Inventory inv = player.getInventory();
        player.getInventory().setArmorContents(null);
        inv.clear();
        player.sendMessage("[BurningCS] Your inventory has been cleared!");
    }

    public void clearInv(Player player, CommandSender cs) {
        Inventory inv = player.getInventory();
        player.getInventory().setArmorContents(null);
        inv.clear();
        if ((cs instanceof Player)) {
            Player sender = (Player) cs;
            player.sendMessage("[BurningCS] Your inventory has been cleared by: " + sender.getDisplayName());
        } else {
            player.sendMessage("[BurningCS] Your inventory has been cleared by: The Console");
        }
    }

    public void loadInventory(Player player, PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode().equals(GameMode.CREATIVE)) {
            try {
                String[] string = BCSInventoryManager.loadStringArray(creativeInv);
                ItemStack[] inventory = new ItemStack[36];
                if (string.length <= 35) {
                    player.sendMessage("Incorrect inventory length! Currently: " + string.length);
                }
                for (int x = 0; x < string.length; ++x) {
                    inventory[x] = invManager.toItemStack(string[x], inventory[x]);
                }
                string = (String[]) BCSInventoryManager.loadStringArray(creativeArmor);
                if (string.length <= 3) {
                    player.sendMessage("Incorrect armor length! Currently: " + string.length);
                }
                ItemStack[] armor = new ItemStack[4];
                for (int x = 0; x < string.length; ++x) {
                    armor[x] = invManager.toItemStack(string[x], armor[x]);
                }
                player.getInventory().setContents(inventory);
                player.getInventory().setArmorContents(armor);
            } catch (FileNotFoundException e) {
                this.clearInv(player);
                player.sendMessage("Could not find the inventory file");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else if (event.getNewGameMode().equals(GameMode.SURVIVAL)) {
            try {
                String[] string = BCSInventoryManager.loadStringArray(survivalInv);
                ItemStack[] inventory = new ItemStack[36];
                if (string.length <= 35) {
                    player.sendMessage("Incorrect inventory length! Currently: " + string.length);
                }
                for (int x = 0; x < string.length; ++x) {
                    inventory[x] = invManager.toItemStack(string[x], inventory[x]);
                }
                string = (String[]) BCSInventoryManager.loadStringArray(survivalArmor);
                if (string.length <= 3) {
                    player.sendMessage("Incorrect armor length! Currently: " + string.length);
                }
                ItemStack[] armor = new ItemStack[4];
                for (int x = 0; x < string.length; ++x) {
                    armor[x] = invManager.toItemStack(string[x], armor[x]);
                }
                player.getInventory().setContents(inventory);
                player.getInventory().setArmorContents(armor);
            } catch (FileNotFoundException e) {
                this.clearInv(player);
                player.sendMessage("Could not find the inventory file");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else {
            player.sendMessage("Something is very wrong here");
        }
    }
}
