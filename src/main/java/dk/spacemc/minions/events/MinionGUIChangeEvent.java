package dk.spacemc.minions.events;

import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MinionGUIChangeEvent implements Listener {

    @EventHandler
    public void MinionGUICloseEvent(InventoryCloseEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
        Minions.getInstance().inventoryManager.remove(player);
    }

    @EventHandler
    public void MinionGUIPickEvent(InventoryClickEvent event) {
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());

        if(!Minions.getInstance().inventoryManager.containsKey(player)) {
            return;
        }
        if(!Minions.getInstance().inventoryManager.get(player).getInventory().equals(event.getInventory())) {
            return;
        }
        event.setCancelled(true);
        Minions.getInstance().inventoryManager.get(player).getLambda().run(event);
    }

}
