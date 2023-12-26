package dk.spacemc.minions;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import dk.spacemc.minions.classes.InventoryData;
import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.commands.MinionCommand;
import dk.spacemc.minions.events.*;
import dk.spacemc.minions.utils.Manager;
import dk.spacemc.minions.utils.Util;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;

public final class Minions extends JavaPlugin {

    private static Minions plugin;
    public Economy economy = null;
    public static HolographicDisplaysAPI api;
    public Manager manager;
    public List<Minion> minions;
    public HashMap<Player, InventoryData> inventoryManager;
    public HashMap<Player, Minion> chestPlacementManager;
    @Override
    public void onEnable() {
        if(!getDataFolder().exists()) getDataFolder().mkdir(); //Lav plugin mappe hvis den ikke eksistere
        plugin = this;
        manager = new Manager(); //Instance af vores database manager
        minions = manager.loadData(); //Load data fra vores database
        inventoryManager = new HashMap<>();
        chestPlacementManager = new HashMap<>();

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
        getServer().getPluginManager().registerEvents(new MinionGUIChangeEvent(), this);
        getServer().getPluginManager().registerEvents(new ChestPlaceEvent(), this);
        getServer().getPluginManager().registerEvents(new ChestBreakEvent(), this);

        getCommand("minion").setExecutor(new MinionCommand());


        new BukkitRunnable() {
            boolean hasLoaded = false;
            @Override
            public void run() {
                boolean temp = true;
                if(!hasLoaded) {
                    for(Minion minion : minions) {
                        if(minion.getWorld() != null) {
                            temp = false;
                            continue;
                        }

                        if(!minion.isSpawned()) {
                            minion.spawn(); //Spawn minions hvis de ikke er spawnet endnu
                        }
                        minion.run();
                    }
                    if(temp) {
                        hasLoaded = true;
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 10, 10);


        new BukkitRunnable() {
            @Override
            public void run() {
                for(Minion minion : minions) {
                    if(!minion.isRunning()) {
                        minion.run();
                    }
                }
            }
        }.runTaskTimer(plugin, 60, 60);



    }

    @Override
    public void onDisable() {
        for(Minion minion : minions) {
            minion.remove(false);
        }
        manager.saveData(minions); //Gem data n\u00E5r serveren lukker
    }

    public static Minions getInstance() {
        return plugin;
    }
}
