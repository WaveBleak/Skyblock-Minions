package dk.spacemc.minions;

import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.utils.Manager;
import dk.spacemc.minions.utils.Util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public final class Minions extends JavaPlugin {

    private static Minions plugin;
    public Economy economy = null;
    public Manager manager;
    public List<Minion> minions;
    @Override
    public void onEnable() {
        if(!getDataFolder().exists()) getDataFolder().mkdir();
        plugin = this;
        manager = new Manager();
        minions = manager.loadData();
        if(Util.setupEconomy()) {
            System.out.println("Economy successfully loaded!");
        } else {
            System.out.println("Economy couldn't load!");
        }


        for(Minion minion : minions) {
            if(!minion.isSpawned()) {
                minion.spawn();
            }
            minion.run();
        }



    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Minions getInstance() {
        return plugin;
    }
}
