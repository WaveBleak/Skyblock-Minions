package dk.spacemc.minions.events;

import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.*;
import dk.spacemc.minions.utils.Util;
import dk.wavebleak.sell.SellManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
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
            if(!minion.getOwner().equals(e.getPlayer())) {
                if(!e.getPlayer().isOp()) {
                    return;
                }
            }
            Minion.minionType type = minion.getType();
            String name;
            String lore1;
            switch (type) {
                case SELL:
                    name = "Sell";
                    lore1 = "&fGenstande solgt: " + minion.getItemsSold();
                    break;

                case ATTACK:
                    name = "Attack";
                    lore1 = "&fV\u00E6sner drabt: " + minion.getEntitiesKilled();
                    break;

                case DIG:
                    name = "Dig";
                    lore1 = "&fBlocks minet: " + minion.getBlocksBroken();
                    break;

                default:
                    name = "Pickup";
                    lore1 = "&fGenstande opsamlet: " + minion.getItemsPickedUp();
                    break;

            }
            String lore2 = "&fTid aktiv: " + Util.formatTime(minion.getSecondsAlive());
            String level = "&fLevel: " + minion.getLevel();

            String levelText = "&f" + minion.calcUpgradeCost() + "$";
            if(minion.getLevel() >= 20) {
                levelText = "&cAllerade max level!";
            }
            Inventory inventory = Bukkit.createInventory(null, InventoryType.DISPENSER, ChatColor.translateAlternateColorCodes('&', "&7> &b" + minion.getOwner().getName() + "'s " + name + " minion"));
            ItemStack skull = Util.setNameAndLore(minion.getSkull(), "&b" + e.getPlayer().getName() + "'s " + name + " minion", lore1, lore2, level);
            ItemStack upgrade = Util.setNameAndLore(new ItemStack(Material.DIAMOND), "&bUpgrade", "&fKlik her for at opgradere din minion!", levelText);
            ItemStack chestSetter = Util.setNameAndLore(new ItemStack(Material.CHEST), "&bSet Chest", "&fKlik her for at forbinde din minion til en chest", "&c(M\u00E5 max v\u00E6re 5 blokke v\u00E6k)");
            ItemStack forceUpgrade = Util.setNameAndLore(new ItemStack(Material.COMMAND), "&cForce Upgrade", "&fKlik her for at opgradere minionen uden det koster penge!");
            ItemStack pickup = Util.setNameAndLore(new ItemStack(Material.BEDROCK), "&cPickup", "&fKlik her for at samle denne minion op");
            ItemStack pane = Util.setNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15), "&8");
            ItemStack kickstart = Util.setNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5), "&aKickstart minion!", "&fKun brug dette hvis din minion ikke arbejder!");

            inventory.setItem(0, pane);
            inventory.setItem(1, skull);
            inventory.setItem(2, pane);

            inventory.setItem(4, upgrade);
            if(!e.getPlayer().isOp()) {
                inventory.setItem(3, pane);
                inventory.setItem(5, pane);
            } else {
                inventory.setItem(3, forceUpgrade);
                inventory.setItem(5, pickup);
            }

            inventory.setItem(6, pane);
            inventory.setItem(7, chestSetter);
            inventory.setItem(8, kickstart);


            e.getPlayer().openInventory(inventory);



            InventoryManager lambda = (InventoryClickEvent event) -> {
                if(event.getCurrentItem() == null) return;
                if(event.getCurrentItem().equals(kickstart)) {
                    minion.run();
                }
                else if(event.getCurrentItem().equals(forceUpgrade) && e.getPlayer().isOp()) {
                    String text = "&fLevel: " + minion.getLevel();
                    if(minion.getLevel() >= 20) {
                        text = "&cAllerade max level!";
                    }
                    ItemStack head = Util.setNameAndLore(minion.getSkull(), "&b" + e.getPlayer().getName() + "'s " + name + " minion", lore1, lore2, text);
                    inventory.setItem(1, head);
                    minion.forceUpgrade();
                }
                else if(event.getCurrentItem().equals(upgrade)) {
                    if(minion.getLevel() >= 20) {
                        return;
                    }
                    if(minion.upgrade()) {
                        e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&aDu opgraderede din minion til level " + minion.getLevel() + "!"));
                        e.getPlayer().closeInventory();
                    } else {
                        e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDu har ikke r\u00E5d til dette!"));
                        e.getPlayer().closeInventory();
                    }
                }
                else if(event.getCurrentItem().equals(pickup) && e.getPlayer().isOp()) {
                    MinionEgg minionEgg = new MinionEgg(minion.getType(), minion.getLevel(), minion.getBlocksBroken(), minion.getEntitiesKilled(), minion.getItemsPickedUp(), minion.getItemsSold(), minion.getSecondsAlive());
                    minion.remove(true);
                    if(hasRoomForItem(minionEgg.getEgg(), e.getPlayer().getInventory())) {
                        e.getPlayer().getInventory().addItem(minionEgg.getEgg());
                    } else {
                        e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), minionEgg.getEgg());
                    }
                }
                else if(event.getCurrentItem().equals(chestSetter)) {
                    event.setCancelled(false);
                    Minions.getInstance().chestPlacementManager.remove(e.getPlayer());
                    event.setCurrentItem(Util.setNameAndLore(new ItemStack(Material.CHEST), "&bTarget Chest", "&fPlacer mig for at s\u00E6tte din minion's kiste"));
                    Minions.getInstance().chestPlacementManager.put(e.getPlayer(), minion);
                }
                else if(!event.getCurrentItem().equals(pane)){
                    event.setCancelled(false);
                }
            };

            Minions.getInstance().inventoryManager.put(e.getPlayer(), new InventoryData(lambda, inventory));
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageByEntityEvent evt) {
        if(!Minion.isMinion(evt.getEntity())) return;
        if(!(evt.getDamager() instanceof Player)) return;
        Player player = (Player) evt.getDamager();
        Minion minion = Minion.getMinion(evt.getEntity());
        if(player == null || minion == null) return;
        evt.setCancelled(true);

        if(!player.equals(minion.getOwner())) return;
        MinionEgg minionEgg = new MinionEgg(minion.getType(), minion.getLevel(), minion.getBlocksBroken(), minion.getEntitiesKilled(), minion.getItemsPickedUp(), minion.getItemsSold(), minion.getSecondsAlive());
        minion.remove(true);
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
