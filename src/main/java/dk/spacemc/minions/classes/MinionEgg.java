package dk.spacemc.minions.classes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.Arrays;
import java.util.Collections;

public class MinionEgg {

    private final ItemStack egg;


    public MinionEgg(Minion.minionType type, int level, int blocksBroken, int entitiesKilled, int itemsPickedUp, int itemsSold, int secondsAlive) {
        ItemStack egg = new ItemStack(Material.MONSTER_EGG, 1, (byte) 4);
        ItemMeta meta = egg.getItemMeta();

        String name;
        switch (type) {
            case SELL:
                name = "Sell";
                break;
            case ATTACK:
                name = "Attack";
                break;
            case DIG:
                name = "Dig";
                break;
            default:
                name = "Pickup";
                break;
        }
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + name + " Minion"));
        meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&8" + type.name()), ChatColor.translateAlternateColorCodes('&', "&8" + level), ChatColor.translateAlternateColorCodes('&', "&8" + blocksBroken), ChatColor.translateAlternateColorCodes('&', "&8" + entitiesKilled), ChatColor.translateAlternateColorCodes('&', "&8" + itemsPickedUp), ChatColor.translateAlternateColorCodes('&', "&8" + itemsSold), ChatColor.translateAlternateColorCodes('&', "&8" + secondsAlive)));

        egg.setItemMeta(meta);

        this.egg = egg;
    }

    public ItemStack getEgg() {
        return egg;
    }
}
