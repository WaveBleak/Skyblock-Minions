package dk.spacemc.minions.events;

import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.classes.MinionEgg;
import dk.wavebleak.sell.SellManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MinionManipulateEvent implements Listener {

    @EventHandler
    public void changeArmorStandEvent(PlayerArmorStandManipulateEvent e) {
        if(Minion.isMinion(e.getRightClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageEvent evt) {
        if(!Minion.isMinion(evt.getEntity())) return;
        evt.setCancelled(true);
    }

    @EventHandler
    public void entityDamage(EntityDamageByEntityEvent evt) {
        if(!Minion.isMinion(evt.getEntity())) return;
        if(!(evt.getDamager() instanceof Player)) return;
        Player player = (Player) evt.getDamager();
        Minion minion = Minion.getMinion(evt.getEntity());
        MinionEgg minionEgg = new MinionEgg(minion.getType());
        evt.setCancelled(true);
        minion.remove();
        SellManager.sendPlayerMessage(player, "&aFjernede din minion!");
        if(hasRoomForItem(minionEgg.getEgg(), player.getInventory())) {
            player.getInventory().addItem(minionEgg.getEgg());
        } else {
            player.getWorld().dropItem(player.getLocation(), minionEgg.getEgg());
        }
    }

    public boolean hasRoomForItem(ItemStack itemStack, Inventory inventory) {

        // Calculate the available space for the item in the chest inventory
        int availableSpace = 0;
        for (ItemStack slot : inventory.getContents()) {
            if (slot == null || slot.getType() == Material.AIR) {
                availableSpace += itemStack.getMaxStackSize();
            } else if (slot.isSimilar(itemStack) && slot.getAmount() < itemStack.getMaxStackSize()) {
                availableSpace += itemStack.getMaxStackSize() - slot.getAmount();
            }
        }
        return availableSpace >= itemStack.getAmount();
    }

}
