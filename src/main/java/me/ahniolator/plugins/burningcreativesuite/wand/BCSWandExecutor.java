package me.ahniolator.plugins.burningcreativesuite.wand;

import me.ahniolator.plugins.burningcreativesuite.BCSConfig;
import me.ahniolator.plugins.burningcreativesuite.BurningCreativeSuite;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BCSWandExecutor implements CommandExecutor {
    private final BurningCreativeSuite plugin;
    private final BCSConfig config;
    
    public BCSWandExecutor(BurningCreativeSuite plugin, BCSConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
