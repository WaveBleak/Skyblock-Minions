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



    public static String formatTime(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds should be a non-negative integer.");
        }

        int weeks = seconds / 604800;
        int days = (seconds % 604800) / 3600;
        int hours = (seconds % 86400) / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        String formattedWeeks = String.valueOf(weeks);
        String formattedDays = String.valueOf(days);
        String formattedHours = String.valueOf(hours);
        String formattedMinutes = String.valueOf(minutes);
        String formattedSeconds = String.valueOf(remainingSeconds);

        String weekText = "uge";
        String dayText = "dag";
        String hourText = "time";
        String minuteText = "minut";
        String secondText = "sekund";

        if(weeks != 1) {
            weekText = "uger";
        }
        if(days != 1) {
            dayText = "dage";
        }
        if(hours != 1) {
            hourText = "timer";
        }
        if(minutes != 1) {
            minuteText = "minutter";
        }
        if(seconds != 1) {
            secondText = "sekunder";
        }


        if(weeks > 0) {
            return formattedWeeks + " " + weekText + ", " + formattedDays + " " + dayText + ", " + formattedHours + " " + hourText + " og " + formattedMinutes + " " + minuteText;
        }else if (days > 0) {
            return formattedDays + " " + dayText + ", " + formattedHours + " " + hourText + " og " + formattedMinutes + " " + minuteText;
        } else if (hours > 0) {
            return formattedHours + " " + hourText + " og " + formattedMinutes + " " + minuteText;
        } else if (minutes > 0) {
            return formattedMinutes + " " + minuteText;
        }else {
            return formattedSeconds + " " + secondText;
        }
    }

}
