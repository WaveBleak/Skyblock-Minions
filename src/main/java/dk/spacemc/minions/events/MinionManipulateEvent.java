package dk.spacemc.minions.events;

import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.classes.MinionEgg;
import dk.spacemc.minions.utils.Util;
import dk.wavebleak.sell.SellManager;
import org.bukkit.Bukkit;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

import static dk.wavebleak.sell.Sell.economy;

public class MinionManipulateEvent implements Listener {

    @EventHandler
    public void changeArmorStandEvent(PlayerArmorStandManipulateEvent e) {
        if(Minion.isMinion(e.getRightClicked())) {
            e.setCancelled(true);
            Minion minion = Minion.getMinion(e.getRightClicked());
            Minion.minionType type = minion.getType();
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
            Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, ChatColor.translateAlternateColorCodes('&', "&7> &b" + e.getPlayer().getName() + "'s " + name + " minion"));
            ItemStack skull = Util.setNameAndLore(minion.getSkull(), " &b" + e.getPlayer().getName() + "'s " + name + " minion", "&fBlah blah", "&gBlah");
            ItemStack upgrade = Util.setNameAndLore(new ItemStack(Material.DIAMOND), "&bUpgrade", "&fKlik her for at opgradere din minion!", "&f" + minion.calcUpgradeCost() + "$");
            inventory.setItem(1, skull);
            inventory.setItem(7, upgrade);

            e.getPlayer().openInventory(inventory);

            Minions.getInstance().inventoryManager.put(e.getPlayer(), minion);
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
