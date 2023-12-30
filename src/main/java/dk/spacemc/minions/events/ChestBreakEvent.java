package dk.spacemc.minions.events;

import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.Minion;
import dk.wavebleak.sell.SellManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ChestBreakEvent implements Listener {

    private boolean test;

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);

        test = false;

        for(Minion minion : Minions.getInstance().minions) {
            Chest chest = minion.getChest();

            if(chest == null) continue;

            if(event.getBlock().equals(chest.getBlock())) {
                SellManager.sendPlayerMessage(event.getPlayer(), "&cDu kan ikke smadre en kiste som er forbundet en minion!");
                test = true;
            }
            if(!event.getBlock().getType().equals(Material.CHEST)) continue;
            if(!(((Chest)event.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest)) continue;

            Location blockLocation = event.getBlock().getLocation();
            int[][] directions = {{-1, 0, 0}, {1, 0, 0}, {0, 0, -1}, {0, 0, 1}};

            for (int[] direction : directions) {
                Location neighbourLocation = blockLocation.clone().add(direction[0], direction[1], direction[2]);
                handleChest(neighbourLocation, chest, event);
            }

        }
        if(!test) {
            event.setCancelled(false);
        }

    }


    public void handleChest(Location location, Chest chest, BlockBreakEvent event) {
        BlockState blockState = location.getBlock().getState();
        if (blockState instanceof Chest) {
            Chest currentChest = (Chest) blockState;
            if (currentChest.getInventory().getHolder() instanceof DoubleChest) {
                if (location.getBlock().equals(chest.getBlock())) {
                    SellManager.sendPlayerMessage(event.getPlayer(), "&cDu kan ikke smadre en kiste som er forbundet en minion!");
                    test = true;
                }
            }
        }
    }
}
