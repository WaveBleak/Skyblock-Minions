package dk.spacemc.minions.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;


import java.util.ArrayList;

import static dk.spacemc.minions.Minions.getInstance;
import static org.bukkit.Bukkit.getServer;

public class Util {

    /**
     * Setup et link til Vault pluginnet
     * @return Om Vault pluginnet findes eller ej
     */
    public static boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if(economyProvider != null) {
            getInstance().economy = economyProvider.getProvider();
        }

        return (getInstance().economy != null);
    }

    public static ItemStack setNameAndLore(ItemStack in, String name, String... lore) {
        ItemMeta meta = in.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        ArrayList<String> loreLines = new ArrayList<>();
        for(String loreLine : lore) {
            loreLines.add(ChatColor.translateAlternateColorCodes('&', loreLine));
        }
        meta.setLore(loreLines);

        in.setItemMeta(meta);
        return in;
    }

}
