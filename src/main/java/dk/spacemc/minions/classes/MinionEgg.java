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

    private ItemStack egg;

    public MinionEgg(Minion.minionType type) {
        ItemStack egg = new ItemStack(Material.MONSTER_EGG);
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
        meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&8" + type.name())));

        egg.setItemMeta(meta);

        this.egg = egg;
    }

    public ItemStack getEgg() {
        return egg;
    }
}
