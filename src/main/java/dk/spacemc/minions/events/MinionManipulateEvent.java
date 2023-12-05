package dk.spacemc.minions.events;

import dk.spacemc.minions.classes.Minion;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class MinionManipulateEvent implements Listener {

    @EventHandler
    public void changeArmorStandEvent(PlayerArmorStandManipulateEvent e) {
        if(Minion.isMinion(e.getRightClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void hitArmorStandEvent(PlayerInteractEntityEvent e) {
        Entity clickedEntity = e.getRightClicked();

        if(Minion.isMinion(clickedEntity)) {
            e.setCancelled(true);
            Minion.getMinion(clickedEntity).remove();
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&aFjernede din minion!"));
        }
    }

}
