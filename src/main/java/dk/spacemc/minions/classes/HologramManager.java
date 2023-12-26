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
     * Hologram constructer
     * @param text Den text hologrammet har, farvet
     * @param location Start lokation
     * @param up Hvor langt op den skal g\u00E5
     * @param small Om det er et lille hologram
     */
    public HologramManager(String text, Location location, double up, boolean small) {
        this.text = text;
        this.location = location;
        this.up = up;
        this.small = small;
        this.lifteTime = 0;
    }

    /**
     * Hologram constructer
     * @param text Den text hologrammet har, farvet
     * @param location Lokation
     * @param lifeTime Hvor mange ticks den skal leve i, 0 hvis den aldrig skal d\u00F8
     * @param small Om det er et lille hologram
     */
    public HologramManager(String text, Location location, int lifeTime, boolean small) {
        this.text = text;
        this.location = location;
        this.up = 0;
        this.small = small;
        this.lifteTime = lifeTime;
    }


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
