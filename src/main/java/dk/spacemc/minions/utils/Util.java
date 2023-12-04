package dk.spacemc.minions.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;


import static dk.spacemc.minions.Minions.getInstance;
import static org.bukkit.Bukkit.getServer;

public class Util {

    public static boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if(economyProvider != null) {
            getInstance().economy = economyProvider.getProvider();
        }

        return (getInstance().economy != null);
    }

}
