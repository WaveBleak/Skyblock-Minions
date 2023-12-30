package dk.spacemc.minions.classes;

import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

import static dk.spacemc.minions.Minions.api;
import static dk.spacemc.minions.Minions.getInstance;

public class HologramManager {

    String text;
    double up;
    int lifteTime;
    boolean small;
    Location location;

    Hologram hologram;


    /**
     * Creates a new HologramManager object.
     *
     * @param text     The text that the hologram will display, can include color codes.
     * @param location The location where the hologram will be spawned.
     * @param up       The distance (in blocks) that the hologram will move upwards. Set to 0 for no movement.
     */
    public HologramManager(String text, Location location, double up) {
        this.text = text;
        this.location = location;
        this.up = up;
        this.lifteTime = 0;
    }

    /**
     * Creates a new HologramManager object.
     *
     * @param text     The text that the hologram will display, can include color codes.
     * @param location The location where the hologram will be spawned.
     */
    public HologramManager(String text, Location location, int lifeTime) {
        this.text = text;
        this.location = location;
        this.up = 0;
        this.lifteTime = lifeTime;
    }


    /**
     * Spawns a hologram at the specified location with the specified text. The hologram can move upwards or have a limited lifetime.
     */
    public void spawn() {
        Hologram hologram = api.createHologram(location);

        this.hologram = hologram;

        hologram.getLines().insertText(0, ChatColor.translateAlternateColorCodes('&', text));



        if(up > 0) {
            new BukkitRunnable() {
                final double targetY = location.getY() + up;
                Location currentLoc = location.clone();
                @Override
                public void run() {
                    currentLoc = currentLoc.add(0, 0.1, 0);
                    hologram.setPosition(currentLoc);
                    if(currentLoc.getY() >= targetY) {
                        hologram.delete();
                        this.cancel();
                    }
                }
            }.runTaskTimer(getInstance(), 4, 4);
        } else if (lifteTime > 0){
            new BukkitRunnable() {
                @Override
                public void run() {
                    hologram.delete();
                }
            }.runTaskLater(getInstance(), lifteTime);
        }
    }

    public void kill() {
        hologram.delete();
    }


}
