package dk.spacemc.minions.events;

import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.utils.Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static dk.spacemc.minions.Minions.getInstance;

public class MinionPlaceEvent implements Listener {

    @EventHandler
    public void onMinionPlace(PlayerInteractEvent e) {
        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if(e.getItem() == null) return;
        if(!e.getItem().getType().equals(Material.MONSTER_EGG)) return;
        if(!e.getClickedBlock().getType().equals(Material.CHEST)) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDu skal placere din minion p\u00E5 en kiste!"));
            return;
        }
        if(!e.getClickedBlock().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 1, 0)).getType().equals(Material.AIR)) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDer er ikke plads til at s\u00E6tte din minion p\u00E5 toppen af kisten!"));
            return;
        }

        if(!Util.canPlaceMinion(e.getPlayer(), e.getClickedBlock().getLocation())) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDu kan ikke placere din minion her!"));
            return;
        }


        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        Minion.minionType type;
        try {
            type = Minion.minionType.valueOf(ChatColor.stripColor(item.getItemMeta().getLore().get(0)));
        }catch (IndexOutOfBoundsException ex) {
            return;
        }

        Location spawnLocation = e.getClickedBlock().getLocation().add(0.5, 1, 0.5);
        Location chestLocation = e.getClickedBlock().getLocation();

        double yaw = player.getLocation().getYaw();
        int level;
        try {
            level = Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(1)));
        }catch (NumberFormatException ex) {
            level = 1;
        }

        Minion minion = new Minion(level, type, player, spawnLocation, chestLocation, yaw);
        minion.addBlocksBroken(Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(2))));
        minion.addEntitiesKilled(Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(3))));
        minion.addItemsPickupUp(Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(4))));
        minion.addItemsSold(Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(5))));
        minion.addSecondsAlive(Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(6))));
        minion.spawn();
        minion.run();
        getInstance().minions.add(minion);
        getInstance().manager.saveData(getInstance().minions);

        if(!player.getGameMode().equals(GameMode.CREATIVE)) {
            item.setAmount(1);
            player.getInventory().remove(item);
        }
    }

}
