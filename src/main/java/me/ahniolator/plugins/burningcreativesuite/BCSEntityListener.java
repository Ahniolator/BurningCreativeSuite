package me.ahniolator.plugins.burningcreativesuite;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

public class BCSEntityListener extends EntityListener {

    private final BurningCreativeSuite plugin;
    private final BCSPlayerListener playerListener;
    private final BCSBlockListener blockListener;
    private final BCSConfig config;
    private final BCSEntityListener entityListener;
    private final BCSInventoryManager invManager;
    private int x, y, z;
    private int xi, yi, zi;
    public static ArrayList<Integer> blockX = new ArrayList();
    public static ArrayList<Integer> blockY = new ArrayList();
    public static ArrayList<Integer> blockZ = new ArrayList();
    public String dataDir, worldDir;
    public String blockFileX, blockFileY, blockFileZ, worldName;
    public File fileX, fileY, fileZ;
    private boolean isComplete = false;

    public BCSEntityListener(BurningCreativeSuite plugin, BCSPlayerListener playerListener, BCSBlockListener blockListener, BCSConfig config, BCSEntityListener entityListener, String dataDir, BCSInventoryManager invManager) {
        this.plugin = plugin;
        this.playerListener = playerListener;
        this.config = config;
        this.blockListener = blockListener;
        this.entityListener = entityListener;
        this.dataDir = dataDir + "blockData" + File.separator;
        this.invManager = invManager;
    }

    @Override
    public void onEndermanPickup(EndermanPickupEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!this.config.yml.getBoolean("Enderman.Disable Pickup", true)) {
            return;
        }
        if (!event.isCancelled()) {
            event.setCancelled(true);
        }
        return;
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!this.config.yml.getBoolean("Creative Players.Placed Blocks Give No Drops.Explosions", true)) {
            return;
        }
        List<Block> blocks = event.blockList();
        worldName = event.getLocation().getWorld().getName();
        if (blockListener.loadedWorld == null) {
            blockListener.loadedWorld = "";
        }
        if (!blockListener.isWorldLoaded(worldName)) {
            if (!blockListener.isOnStart) {
                try {
                    if (!blockListener.fileX.exists()) {
                        this.invManager.createNewFile(blockListener.fileX, blockListener.worldDir);
                    }
                    save(BCSBlockListener.blockX, blockListener.blockFileX);
                    if (!blockListener.fileY.exists()) {
                        this.invManager.createNewFile(blockListener.fileY, blockListener.worldDir);
                    }
                    save(BCSBlockListener.blockY, blockListener.blockFileY);
                    if (!blockListener.fileZ.exists()) {
                        this.invManager.createNewFile(blockListener.fileZ, blockListener.worldDir);
                    }
                    save(BCSBlockListener.blockZ, blockListener.blockFileZ);
                    System.out.println("[BurningCS] Saved block data!");
                } catch (Exception ex) {
                    System.out.println("[BurningCS] Failed to save block data!");
                    ex.printStackTrace();
                }
            }
            if (blockListener.isOnStart) {
                blockListener.isOnStart = false;
            }
            blockListener.worldDir = blockListener.dataDir + worldName + File.separator;
            blockListener.blockFileX = blockListener.worldDir + "blocksX.data";
            blockListener.blockFileY = blockListener.worldDir + "blocksY.data";
            blockListener.blockFileZ = blockListener.worldDir + "blocksZ.data";
            blockListener.fileX = new File(blockListener.blockFileX);
            blockListener.fileY = new File(blockListener.blockFileY);
            blockListener.fileZ = new File(blockListener.blockFileZ);
            try {
                BCSBlockListener.blockX = (ArrayList<Integer>) load(blockListener.blockFileX);
                BCSBlockListener.blockY = (ArrayList<Integer>) load(blockListener.blockFileY);
                BCSBlockListener.blockZ = (ArrayList<Integer>) load(blockListener.blockFileZ);
                blockListener.loadedWorld = worldName;
                System.out.println("[BurningCS] Loaded block data!");
            } catch (EOFException e) {
            } catch (Exception e) {
            }
        }
        try {
            for (Block block : blocks) {
                y = block.getLocation().getBlockY();
                x = block.getLocation().getBlockX();
                z = block.getLocation().getBlockZ();
                if (BCSBlockListener.blockX.contains(x) && BCSBlockListener.blockY.contains(y) && BCSBlockListener.blockZ.contains(z)) {
                    if (!isComplete) {
                        System.out.println("[BurningCS] Explosion was going to destroy a CREATIVE block. Unsupported, cancelling explosion");
                        event.setCancelled(true);
                        break;
                    }
                    if (!this.config.yml.getBoolean("Creative Players.Placed Blocks Give No Drops", true)) {
                        return;
                    }
                    xi = BCSBlockListener.blockX.lastIndexOf(x);
                    BCSBlockListener.blockX.remove(xi);
                    yi = BCSBlockListener.blockY.lastIndexOf(y);
                    BCSBlockListener.blockY.remove(yi);
                    zi = BCSBlockListener.blockZ.lastIndexOf(z);
                    BCSBlockListener.blockZ.remove(zi);
                    block.setType(Material.AIR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event instanceof EntityDamageByEntityEvent) || this.config.yml.getBoolean("Creative Players.Attack other entities", true)) {
            return;
        }
        EntityDamageByEntityEvent nEvent = (EntityDamageByEntityEvent) event;
        if (!(nEvent.getDamager() instanceof Player) && !(nEvent.getDamager() instanceof Arrow)) {
            return;
        }
        if (nEvent.getDamager() instanceof Arrow) {
            Arrow a = (Arrow) nEvent.getDamager();
            if (!(a.getShooter() instanceof Player)) {
                return;
            }
            Player player = (Player) a.getShooter();
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                player.sendMessage("[BurningCS] You are not allowed to attack other entities in");
                player.sendMessage("[BurningCS] Creative Mode!");
                event.setCancelled(true);
                return;
            }
            return;
        } else if (nEvent.getDamager() instanceof Player) {
            Player player = (Player) nEvent.getDamager();
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                return;
            }
            player.sendMessage("[BurningCS] You are not allowed to attack other entities in");
            player.sendMessage("[BurningCS] Creative Mode!");
            event.setCancelled(true);
            return;
        } else {
            return;
        }
    }

    @Override
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getTarget() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getTarget();
        if (player.getGameMode().equals(GameMode.CREATIVE) && !this.config.yml.getBoolean("Creative Players.Attack other entities", true)) {
            event.setCancelled(true);
        }
        return;
    }
}