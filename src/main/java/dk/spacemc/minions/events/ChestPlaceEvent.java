package dk.spacemc.minions.events;

import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.utils.Util;
import dk.wavebleak.sell.SellManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ChestPlaceEvent implements Listener {

    @EventHandler
    public void onMinionPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
        ItemStack chest = Util.setNameAndLore(new ItemStack(Material.CHEST), "&bTarget Chest", "&fPlacer mig for at s\u00E6tte din minion's kiste");

        if(!Minions.getInstance().chestPlacementManager.containsKey(e.getPlayer())) {
            e.setCancelled(false);
            return;
        }

        if(!e.getItemInHand().isSimilar(chest)) {
            e.setCancelled(false);
            return;
        }

        Minion minion = Minions.getInstance().chestPlacementManager.get(e.getPlayer());

        if(minion == null) {
            e.setCancelled(false);
            return;
        }

        if(e.getBlockPlaced().getLocation().distance(minion.getMinion().getLocation()) > 5) {
            SellManager.sendPlayerMessage(e.getPlayer(), "&cDu kan max s\u00E6tte denne kiste 5 blocks v\u00E6k");
            return;
        }

        e.getPlayer().getInventory().setItemInHand(new ItemStack(Material.AIR));
        e.setCancelled(false);
        minion.setChestLoc(e.getBlockPlaced().getLocation());
        Minions.getInstance().chestPlacementManager.remove(e.getPlayer());


    }

}
