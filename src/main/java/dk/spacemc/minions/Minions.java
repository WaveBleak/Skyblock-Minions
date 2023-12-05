package dk.spacemc.minions;

import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.commands.MinionCommand;
import dk.spacemc.minions.events.MinionManipulateEvent;
import dk.spacemc.minions.events.MinionPlaceEvent;
import dk.spacemc.minions.utils.Manager;
import dk.spacemc.minions.utils.Util;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public final class Minions extends JavaPlugin {

    private static Minions plugin;
    public Economy economy = null;
    public static HolographicDisplaysAPI api;
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
        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }
        api = HolographicDisplaysAPI.get(this);

        getServer().getPluginManager().registerEvents(new MinionManipulateEvent(), this);
        getServer().getPluginManager().registerEvents(new MinionPlaceEvent(), this);

        getCommand("minion").setExecutor(new MinionCommand());


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
