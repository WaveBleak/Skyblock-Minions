package dk.spacemc.minions.events;

import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.Minion;
import dk.wavebleak.sell.SellManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ChestBreakEvent implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);

        boolean test = false;

        for(Minion minion : Minions.getInstance().minions) {
            Chest chest = minion.getChest();

            if(chest == null) continue;

            if(event.getBlock().equals(chest.getBlock())) {
                SellManager.sendPlayerMessage(event.getPlayer(), "&cDu kan ikke smadre en kiste som er forbundet en minion!");
                test = true;
            }
            if(!event.getBlock().getType().equals(Material.CHEST)) continue;
            if(!(((Chest)event.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest)) continue;

            Location location1 = event.getBlock().getLocation().add(-1, 0, 0);
            Location location2 = event.getBlock().getLocation().add(1, 0, 0);
            Location location3 = event.getBlock().getLocation().add(0, 0, -1);
            Location location4 = event.getBlock().getLocation().add(0, 0, 1);

            if(((Chest)location1.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest) {
                if(location1.getBlock().equals(chest.getBlock())) {
                    SellManager.sendPlayerMessage(event.getPlayer(), "&cDu kan ikke smadre en kiste som er forbundet en minion!");
                    test = true;
                }
            }
            if(((Chest)location2.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest) {
                if(location2.getBlock().equals(chest.getBlock())) {
                    SellManager.sendPlayerMessage(event.getPlayer(), "&cDu kan ikke smadre en kiste som er forbundet en minion!");
                    test = true;
                }
            }
            if(((Chest)location3.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest) {
                if(location3.getBlock().equals(chest.getBlock())) {
                    SellManager.sendPlayerMessage(event.getPlayer(), "&cDu kan ikke smadre en kiste som er forbundet en minion!");
                    test = true;
                }
            }
            if(((Chest)location4.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest) {
                if(location4.getBlock().equals(chest.getBlock())) {
                    SellManager.sendPlayerMessage(event.getPlayer(), "&cDu kan ikke smadre en kiste som er forbundet en minion!");
                    test = true;
                }
            }
        }
        if(!test) {
            event.setCancelled(false);
        }

    }

}
