package me.ahniolator.plugins.burningcreativesuite.wand;

import me.ahniolator.plugins.burningcreativesuite.BCSConfig;
import me.ahniolator.plugins.burningcreativesuite.BurningCreativeSuite;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class BCSWandPlayerListener extends PlayerListener {
    private final BurningCreativeSuite plugin;
    private final BCSConfig config;
    
    public BCSWandPlayerListener(BurningCreativeSuite plugin, BCSConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        return;
    }
    
}
