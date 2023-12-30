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
        ItemMeta eggMeta = egg.getItemMeta();

        String minionName = getMinionName(type);
        eggMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + minionName + " Minion"));

        eggMeta.setLore(Arrays.asList(generateLore("&8", type.name()),
                generateLore("&8", Integer.toString(level)),
                generateLore("&8", Integer.toString(blocksBroken)),
                generateLore("&8", Integer.toString(entitiesKilled)),
                generateLore("&8", Integer.toString(itemsPickedUp)),
                generateLore("&8", Integer.toString(itemsSold)),
                generateLore("&8", Integer.toString(secondsAlive))));

        egg.setItemMeta(eggMeta);
        this.egg = egg;
    }

    private String getMinionName(Minion.minionType type) {
        switch (type) {
            case SELL:
                return "Sell";
            case ATTACK:
                return "Attack";
            case DIG:
                return "Dig";
            default:
                return "Pickup";
        }
    }

    private String generateLore(String colorCode, String inputString) {
        return ChatColor.translateAlternateColorCodes('&', colorCode + inputString);
    }

    public ItemStack getEgg() {
        return egg;
    }
}
