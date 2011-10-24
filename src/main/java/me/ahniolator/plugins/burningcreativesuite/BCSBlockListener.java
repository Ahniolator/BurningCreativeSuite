package me.ahniolator.plugins.burningcreativesuite;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BCSBlockListener extends BlockListener {

    public String worldName, loadedWorld;
    private File blockFile;
    private final BurningCreativeSuite plugin;
    private final BCSPlayerListener playerListener;
    private final BCSBlockListener blockListener;
    private final BCSConfig config;
    private final BCSEntityListener entityListener;
    private final BCSInventoryManager invManager;
    public int x, y, z;
    public int xi, yi, zi;
    private int blocksNum = 0;
    private final ArrayList<Material> breakingMats = new ArrayList();
    public static ArrayList<Integer> blockX = new ArrayList();
    public static ArrayList<Integer> blockY = new ArrayList();
    public static ArrayList<Integer> blockZ = new ArrayList();
    public String dataDir, worldDir;
    public String blockFileX, blockFileY, blockFileZ;
    public File fileX, fileY, fileZ;
    public boolean isOnStart = true, devMode = false, isDoor = false, hasSaved = false;
    private long starttime, stoptime;

    public BCSBlockListener(BurningCreativeSuite plugin, BCSPlayerListener playerListener, BCSBlockListener blockListener, BCSConfig config, BCSEntityListener entityListener, String dataDir, BCSInventoryManager invManager) {
        this.plugin = plugin;
        this.playerListener = playerListener;
        this.config = config;
        this.blockListener = blockListener;
        this.entityListener = entityListener;
        this.dataDir = dataDir + "blockData" + File.separator;
        this.invManager = invManager;
        this.breakingMats.add(Material.TORCH);
        this.breakingMats.add(Material.REDSTONE_TORCH_OFF);
        this.breakingMats.add(Material.REDSTONE_TORCH_ON);
        this.breakingMats.add(Material.STONE_BUTTON);
        this.breakingMats.add(Material.LEVER);
        this.breakingMats.add(Material.WOOD_PLATE);
        this.breakingMats.add(Material.STONE_PLATE);
        this.breakingMats.add(Material.RAILS);
        this.breakingMats.add(Material.POWERED_RAIL);
        this.breakingMats.add(Material.DETECTOR_RAIL);
        this.breakingMats.add(Material.REDSTONE_WIRE);
        this.breakingMats.add(Material.DIODE_BLOCK_OFF);
        this.breakingMats.add(Material.DIODE_BLOCK_ON);
        this.breakingMats.add(Material.YELLOW_FLOWER);
        this.breakingMats.add(Material.RED_ROSE);
        this.breakingMats.add(Material.RED_MUSHROOM);
        this.breakingMats.add(Material.BROWN_MUSHROOM);
        this.breakingMats.add(Material.SIGN);
        this.breakingMats.add(Material.SIGN_POST);
        this.breakingMats.add(Material.WOODEN_DOOR);
        this.breakingMats.add(Material.IRON_DOOR_BLOCK);
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.devMode) {
            this.starttime = System.currentTimeMillis();
        }
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, block)) {
            return;
        }
        int oldBlockType = block.getTypeId();
        byte oldBlockData = block.getData();
        try {
            y = block.getLocation().getBlockY();
            if (block.getType() == Material.BEDROCK && block.getLocation().getBlockY() <= 5 && !player.hasPermission("bcs.breakbedrock")) {
                if (!this.config.yml.getBoolean("Creative Players.Disable Bottom-of-the-World Bedrock Break", true)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            x = block.getLocation().getBlockX();
            z = block.getLocation().getBlockZ();
            if (block.getType().equals(Material.WOODEN_DOOR) && block.getRelative(BlockFace.DOWN).getType().equals(Material.WOODEN_DOOR)) {
                block = block.getRelative(BlockFace.DOWN);
                x = block.getLocation().getBlockX();
                y = block.getLocation().getBlockY();
                z = block.getLocation().getBlockZ();
                this.isDoor = true;
            } else if (block.getType().equals(Material.IRON_DOOR_BLOCK) && block.getRelative(BlockFace.DOWN).getType().equals(Material.IRON_DOOR_BLOCK)) {
                block = block.getRelative(BlockFace.DOWN);
                x = block.getLocation().getBlockX();
                y = block.getLocation().getBlockY();
                z = block.getLocation().getBlockZ();
                this.isDoor = true;
            }
            worldName = block.getWorld().getName();
            if (loadedWorld == null) {
                loadedWorld = "";
            }
            if (!this.isWorldLoaded(worldName)) {
                if (!isOnStart) {
                    try {
                        if (!this.fileX.exists()) {
                            this.invManager.createNewFile(this.fileX, this.worldDir);
                        }
                        save(blockX, this.blockFileX);
                        if (!this.fileY.exists()) {
                            this.invManager.createNewFile(this.fileY, this.worldDir);
                        }
                        save(blockY, this.blockFileY);
                        if (!this.fileZ.exists()) {
                            this.invManager.createNewFile(this.fileZ, this.worldDir);
                        }
                        save(blockZ, this.blockFileZ);
                        this.hasSaved = true;
                    } catch (Exception ex) {
                        System.out.println("[BurningCS] Failed to save block data!");
                        ex.printStackTrace();
                    }
                }
                if (this.isOnStart) {
                    isOnStart = false;
                }
                worldDir = dataDir + worldName + File.separator;
                blockFileX = worldDir + "blocksX.data";
                blockFileY = worldDir + "blocksY.data";
                blockFileZ = worldDir + "blocksZ.data";
                fileX = new File(blockFileX);
                fileY = new File(blockFileY);
                fileZ = new File(blockFileZ);
                try {
                    blockX = (ArrayList<Integer>) load(this.blockFileX);
                    blockY = (ArrayList<Integer>) load(this.blockFileY);
                    blockZ = (ArrayList<Integer>) load(this.blockFileZ);
                    this.loadedWorld = worldName;
                } catch (EOFException e) {
                } catch (Exception e) {
                }
            }
            if (blockX.contains(x) && blockY.contains(y) && blockZ.contains(z)) {
                if (this.devMode) {
                    player.sendMessage("You broke a block placed in creative mode!");
                }
                if (!player.hasPermission("bcs.bypass.blockbreak") && this.config.yml.getBoolean("Creative Players.Placed Blocks Give No Drops.Players", true)) {
                    if (hasSaved) {
                        worldDir = dataDir + worldName + File.separator;
                        blockFileX = worldDir + "blocksX.data";
                        blockFileY = worldDir + "blocksY.data";
                        blockFileZ = worldDir + "blocksZ.data";
                        fileX = new File(blockFileX);
                        fileY = new File(blockFileY);
                        fileZ = new File(blockFileZ);
                        loadBlockData(this.blockFileX, this.blockFileY, this.blockFileZ, worldName);
                    }
                    event.setCancelled(true);
                    Block block2 = block.getRelative(BlockFace.UP);
                    if (this.breakingMats.contains(block2.getType())
                            && blockX.contains(block2.getLocation().getBlockX())
                            && blockY.contains(block2.getLocation().getBlockY())
                            && blockZ.contains(block2.getLocation().getBlockZ())) {
                        if (block2.getType().equals(Material.TORCH) || block2.getType().equals(Material.REDSTONE_TORCH_OFF) || block2.getType().equals(Material.REDSTONE_TORCH_ON)) {
                            if (block2.getData() == 5) {
                                block2.setType(Material.AIR);
                                xi = blockX.lastIndexOf(block2.getLocation().getBlockX());
                                blockX.remove(xi);
                                yi = blockY.lastIndexOf(block2.getLocation().getBlockY());
                                blockY.remove(yi);
                                zi = blockZ.lastIndexOf(block2.getLocation().getBlockZ());
                                blockZ.remove(zi);
                            }
                        } else if ((block2.getType().equals(Material.WOODEN_DOOR) && block2.getRelative(BlockFace.UP).getType().equals(Material.WOODEN_DOOR))
                                || (block2.getType().equals(Material.IRON_DOOR_BLOCK) && block2.getRelative(BlockFace.UP).getType().equals(Material.IRON_DOOR_BLOCK))) {
                            block2.setType(Material.AIR);
                            xi = blockX.lastIndexOf(block2.getLocation().getBlockX());
                            blockX.remove(xi);
                            yi = blockY.lastIndexOf(block2.getLocation().getBlockY());
                            blockY.remove(yi);
                            zi = blockZ.lastIndexOf(block2.getLocation().getBlockZ());
                            blockZ.remove(zi);
                            block2.getRelative(BlockFace.UP).setType(Material.AIR);
                        } else if (block2.getType() != Material.WOODEN_DOOR && block2.getType() != Material.IRON_DOOR_BLOCK) {
                            block2.setType(Material.AIR);
                            xi = blockX.lastIndexOf(block2.getLocation().getBlockX());
                            blockX.remove(xi);
                            yi = blockY.lastIndexOf(block2.getLocation().getBlockY());
                            blockY.remove(yi);
                            zi = blockZ.lastIndexOf(block2.getLocation().getBlockZ());
                            blockZ.remove(zi);
                        } else {
                            block2.setType(Material.AIR);
                        }
                    }
                    if (this.isDoor) {
                        block.setType(Material.AIR);
                        xi = blockX.lastIndexOf(x);
                        blockX.remove(xi);
                        yi = blockY.lastIndexOf(y);
                        blockY.remove(yi);
                        zi = blockZ.lastIndexOf(z);
                        blockZ.remove(zi);
                        block.getRelative(BlockFace.UP).setType(Material.AIR);
                        this.isDoor = false;
                    } else {
                        block.setType(Material.AIR);
                        xi = blockX.lastIndexOf(x);
                        blockX.remove(xi);
                        yi = blockY.lastIndexOf(y);
                        blockY.remove(yi);
                        zi = blockZ.lastIndexOf(z);
                        blockZ.remove(zi);
                    }
                    this.blocksNum++;
                    if (this.blocksNum >= this.config.yml.getInt("Blocks Save Interval", 15)) {
                        try {
                            if (!this.fileX.exists()) {
                                this.invManager.createNewFile(this.fileX, this.worldDir);
                            }
                            save(blockX, this.blockFileX);
                            if (!this.fileY.exists()) {
                                this.invManager.createNewFile(this.fileY, this.worldDir);
                            }
                            save(blockY, this.blockFileY);
                            if (!this.fileZ.exists()) {
                                this.invManager.createNewFile(this.fileZ, this.worldDir);
                            }
                            save(blockZ, this.blockFileZ);
                            this.hasSaved = true;
                            this.blocksNum = 0;
                        } catch (Exception ex) {
                            System.out.println("[BurningCS] Failed to save block data!");
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (event.isCancelled() && plugin.logBlock != null) {
                plugin.logBlock.getConsumer().queueBlockBreak(player.getName(), block.getLocation(), oldBlockType, oldBlockData);
            }
        }
        if (this.devMode) {
            this.stoptime = System.currentTimeMillis();
            event.getPlayer().sendMessage("[BurningCS] Event: Block Break took " + (this.stoptime - this.starttime) + "ms to operate.");
        }
        return;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.devMode) {
            this.starttime = System.currentTimeMillis();
        }
        if (event.isCancelled()) {
            return;
        }
        if (event.getBlock().getType().equals(Material.FIRE) || event.getBlock().getType().equals(Material.PORTAL)) {
            return;
        }
        if (!this.config.yml.getBoolean("Creative Players.Placed Blocks Give No Drops.Players", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission("bcs.bypass.blockplace")) {
            return;
        }
        if (hasSaved) {
            worldDir = dataDir + worldName + File.separator;
            blockFileX = worldDir + "blocksX.data";
            blockFileY = worldDir + "blocksY.data";
            blockFileZ = worldDir + "blocksZ.data";
            fileX = new File(blockFileX);
            fileY = new File(blockFileY);
            fileZ = new File(blockFileZ);
            loadBlockData(this.blockFileX, this.blockFileY, this.blockFileZ, worldName);
        }
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            Block block = event.getBlock();
            if (block.getType().equals(Material.SAND)
                    || block.getType().equals(Material.GRAVEL)
                    || block.getType().equals(Material.MINECART)
                    || block.getType().equals(Material.STORAGE_MINECART)
                    || block.getType().equals(Material.POWERED_MINECART)
                    || block.getType().equals(Material.BED_BLOCK)
                    || block.getType().equals(Material.TNT)) {
                return;
            }
            try {
                x = block.getLocation().getBlockX();
                y = block.getLocation().getBlockY();
                z = block.getLocation().getBlockZ();
                worldName = block.getWorld().getName();
                if (loadedWorld == null) {
                    loadedWorld = "";
                }
                if (!this.isWorldLoaded(worldName)) {
                    if (!this.isOnStart) {
                        try {
                            if (!this.fileX.exists()) {
                                this.invManager.createNewFile(this.fileX, this.worldDir);
                            }
                            save(blockX, this.blockFileX);
                            if (!this.fileY.exists()) {
                                this.invManager.createNewFile(this.fileY, this.worldDir);
                            }
                            save(blockY, this.blockFileY);
                            if (!this.fileZ.exists()) {
                                this.invManager.createNewFile(this.fileZ, this.worldDir);
                            }
                            save(blockZ, this.blockFileZ);
                            this.hasSaved = true;
                        } catch (Exception ex) {
                            System.out.println("[BurningCS] Failed to save block data!");
                            ex.printStackTrace();
                        }
                    }
                    if (this.isOnStart) {
                        isOnStart = false;
                    }
                    worldDir = dataDir + worldName + File.separator;
                    blockFileX = worldDir + "blocksX.data";
                    blockFileY = worldDir + "blocksY.data";
                    blockFileZ = worldDir + "blocksZ.data";
                    fileX = new File(blockFileX);
                    fileY = new File(blockFileY);
                    fileZ = new File(blockFileZ);
                    if (!this.fileX.exists()) {
                        new File(this.worldDir).mkdirs();
                        this.fileX.createNewFile();
                    }
                    if (!this.fileY.exists()) {
                        new File(this.worldDir).mkdirs();
                        this.fileY.createNewFile();
                    }
                    if (!this.fileZ.exists()) {
                        new File(this.worldDir).mkdirs();
                        this.fileZ.createNewFile();
                    }
                    try {
                        blockX = (ArrayList<Integer>) load(this.blockFileX);
                        blockY = (ArrayList<Integer>) load(this.blockFileY);
                        blockZ = (ArrayList<Integer>) load(this.blockFileZ);
                        this.loadedWorld = worldName;
                    } catch (EOFException e) {
                    } catch (Exception e) {
                    }
                }
                blockX.add(x);
                blockY.add(y);
                blockZ.add(z);
                this.blocksNum++;
                if (this.blocksNum >= this.config.yml.getInt("Blocks Save Interval", 15)) {
                    try {
                        if (!this.fileX.exists()) {
                            this.invManager.createNewFile(this.fileX, this.worldDir);
                        }
                        save(blockX, this.blockFileX);
                        if (!this.fileY.exists()) {
                            this.invManager.createNewFile(this.fileY, this.worldDir);
                        }
                        save(blockY, this.blockFileY);
                        if (!this.fileZ.exists()) {
                            this.invManager.createNewFile(this.fileZ, this.worldDir);
                        }
                        save(blockZ, this.blockFileZ);
                        this.hasSaved = true;
                        this.blocksNum = 0;
                    } catch (Exception ex) {
                        System.out.println("[BurningCS] Failed to save block data!");
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        if (this.devMode) {
            this.stoptime = System.currentTimeMillis();
            player.sendMessage("[BurningCS] Event: Block Place took " + (this.stoptime - this.starttime) + "ms to operate with " + blockX.size() + " blocks in the database.");
        }
        return;
    }

    public static void save(Object obj, String path) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(obj);
        oos.flush();
        oos.close();
    }

    public static Object load(String path) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    public boolean isWorldLoaded(String world) {
        if (world.equalsIgnoreCase(loadedWorld)) {
            return true;
        } else {
            return false;
        }
    }

    public void saveBlockData() {
        try {
            if (!isOnStart) {
                if (!this.fileX.exists()) {
                    this.invManager.createNewFile(this.fileX, this.worldDir);
                }
                save(blockX, this.blockFileX);
                if (!this.fileY.exists()) {
                    this.invManager.createNewFile(this.fileY, this.worldDir);
                }
                save(blockY, this.blockFileY);
                if (!this.fileZ.exists()) {
                    this.invManager.createNewFile(this.fileZ, this.worldDir);
                }
                save(blockZ, this.blockFileZ);
            }
        } catch (Exception ex) {
            System.out.println("[BurningCS] Failed to save block data!");
            ex.printStackTrace();
        }
    }

    private void loadBlockData(String x, String y, String z, String wn) {
        try {
            blockX = (ArrayList<Integer>) load(this.blockFileX);
            blockY = (ArrayList<Integer>) load(this.blockFileY);
            blockZ = (ArrayList<Integer>) load(this.blockFileZ);
            this.loadedWorld = wn;
            hasSaved = false;
        } catch (EOFException e) {
        } catch (Exception e) {
        }
    }
}
