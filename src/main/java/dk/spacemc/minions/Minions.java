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
        if(!getDataFolder().exists()) getDataFolder().mkdir(); //Lav plugin mappe hvis den ikke eksistere
        plugin = this;
        manager = new Manager(); //Instance af vores database manager
        minions = manager.loadData(); //Load data fra vores database
        if(Util.setupEconomy()) { //Setup economy hvis det findes
            System.out.println("Economy successfully loaded!");
        } else {
            System.out.println("Economy couldn't load!");
        }


        for(Minion minion : minions) {
            if(!minion.isSpawned()) {
                minion.spawn(); //Spawn minions hvis de ikke er spawnet endnu
            }
            minion.run(); //Start alle minions
        }



    }

    @Override
    public void onDisable() {
        manager.saveData(minions); //Gem data når serveren lukker
    }

    public static Minions getInstance() {
        return plugin;
    }
}
