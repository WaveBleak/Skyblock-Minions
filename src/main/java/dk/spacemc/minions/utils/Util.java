package dk.spacemc.minions.utils;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;


import java.util.ArrayList;
import java.util.List;

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
        seconds %= 604800;
        int days = seconds / 86400;
        seconds %= 86400;
        int hours = seconds / 3600;
        seconds %= 3600;
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        StringBuilder result = new StringBuilder();
        if(weeks > 0) {
            result.append(weeks).append(weeks > 1 ? " uger, " : " uge, ");
        }
        if (days > 0) {
            result.append(days).append(days > 1 ? " dage, " : " dag, ");
        }
        if (hours > 0) {
            result.append(hours).append(hours > 1 ? " timer og " : " time og ");
        }
        if (minutes > 0) {
            result.append(minutes).append(minutes > 1 ? " minutter" : " minut");
        } else {
            result.append(remainingSeconds).append(remainingSeconds > 1 ? " sekunder" : " sekund");
        }
        return result.toString();
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        IChatBaseComponent titleComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', title) + "\"}");
        IChatBaseComponent subtitleComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', subtitle) + "\"}");

        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleComponent);
        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitleComponent);
        PacketPlayOutTitle lengthPacket = new PacketPlayOutTitle(fadeIn, stay, fadeOut);

        craftPlayer.getHandle().playerConnection.sendPacket(titlePacket);
        craftPlayer.getHandle().playerConnection.sendPacket(subtitlePacket);
        craftPlayer.getHandle().playerConnection.sendPacket(lengthPacket);
    }


    public static boolean canPlaceMinion(Player player, Location location) {
        if(player.isOp()) return true;

        Island island = SuperiorSkyblockAPI.getIslandAt(location);

        if(island == null) return false;

        List<SuperiorPlayer> members = island.getIslandMembers(true);


        if(members.stream().anyMatch(x -> {
            Player memberPlayer = Bukkit.getPlayer(x.getUniqueId());
            if(memberPlayer.equals(player)) {
                return true;
            } else {
                return false;
            }
        })) {
            return true;
        }
        return false;
    }


}
