package dk.spacemc.minions.classes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import dk.spacemc.minions.Minions;
import javafx.beans.property.Property;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;


import static dk.spacemc.minions.Minions.getInstance;

public class Minion {
    public enum minionType {
        DIG,
        ATTACK,
        SELL,
        PICKUP
    }
    private int level;
    private final int type;
    private final String uuidOfOwner;
    private final double x;
    private final double y;
    private final double z;
    private final double chestX;
    private final double chestY;
    private final double chestZ;
    private final String world;
    private boolean isDisabled;


    /**
     * Constructor for minions
     * @param level Minionens level
     * @param type Minionens type
     * @param uuidOfOwner Ejerens UUID
     * @param x x-koordinatet af minionen
     * @param y y-koordinatet af minionen
     * @param z z-koordinatet af minionen
     * @param chestX x-koordinatet af minionen's associeret kiste
     * @param chestY y-koordinatet af minionen's associeret kiste
     * @param chestZ z-koordinatet af minionen's associeret kiste
     * @param world navnet på verdenen minionen er i
     */
    public Minion(int level, int type, String uuidOfOwner, double x, double y, double z, double chestX, double chestY, double chestZ, String world) {
        this.level = level;
        this.type = type;
        this.uuidOfOwner = uuidOfOwner;
        this.x = x;
        this.y = y;
        this.z = z;
        this.chestX = chestX;
        this.chestY = chestY;
        this.chestZ = chestZ;
        this.world = world;
        this.isDisabled = false;
    }

    public void run() {
        if(taskManager != null) {
            taskManager.cancel(); // Undgå flere tasks at køre på samme tid
        }
        if(animationManager != null) {
            animationManager.cancel();
        }

        taskManager = new BukkitRunnable() {
            @Override
            public void run() {
                if(isDisabled) this.cancel();
                performAction();
            }
        }.runTaskTimer(Minions.getInstance(), getCooldown(), getCooldown());


        animationManager = new BukkitRunnable() {
            private int frames = 1;
            @Override
            public void run() {
                if(frames > 4) frames = 1;
                performAnimation(frames);
                frames++;

            }
        }.runTaskTimer(getInstance(), getCooldown() / 4, getCooldown() / 4);
    }

    private BukkitTask taskManager = null;
    private BukkitTask animationManager = null;


    public void performAction() {
        switch (getType()) {
            case ATTACK:
                attack();
                break;
            case DIG:
                dig();
                break;
            case PICKUP:
                pickup();
                break;
            default:
                sell();
                break;
        }
    }

    /**
     * Sæt armen på minionen i en særlig position
     * @param frame Den frame, der er igang
     */
    public void performAnimation(int frame) {
        ArmorStand minion = getMinion();
        switch (frame) {
            case 1:
                // Arm er højt oppe
                break;
            case 2:
                // Arm er lidt oppe
                break;
            case 3:
                // Arm er lidt nede
                break;
            case 4:
                // Arm er langt nede
                break;
        }
    }

    /**
     * Denne funktion bliver kaldt hvis det er en DIG minion, hver calcCooldown ticks
     */
    public void dig() {
        ArmorStand minion = getMinion();
    }

    /**
     * Denne funktion bliver kaldt hvis det er en PICKUP minion, hver calcCooldown ticks
     */
    public void pickup() {
        Chest chest = getChest();
        ArmorStand minion = getMinion();
    }

    /**
     * Denne funktion bliver kaldt hvis det er en ATTACK minion, hver calcCooldown ticks
     */
    public void attack() {
        ArmorStand minion = getMinion();
    }

    /**
     * Denne funktion bliver kaldt hvis det er en SELL minion, hver calcCooldown ticks
     */
    public void sell() {
        Chest chest = getChest();

    }



    public Chest getChest() {
        Location chestLocation = new Location(getWorld(), chestX, chestY, chestZ);
        return (Chest) getWorld().getBlockAt(chestLocation);
    }

    public boolean isSpawned() {
        return (getMinion() != null);
    }

    /**
     * Opgradere minionen for penge
     * @return true hvis den successfully opgradere(aka: man har råd)
     */
    public boolean upgrade() {
        if(getInstance().economy.bankHas(getOwner().getName(), calcUpgradeCost()).transactionSuccess()) {
            level++;
            run();
            getInstance().economy.withdrawPlayer(getOwner(), calcUpgradeCost());
            return true;
        }
        return false;
    }

    public int calcUpgradeCost() {
        return (int) Math.round(level * 5.5);
    }


    /**
     * Spawner en minion ved dens gemte lokation, giver den average skin farve som lædder farve og hovdet af ejeren
     */
    public void spawn() {
        Location spawnLocation = new Location(getWorld(), x, y, z);
        ArmorStand minion = (ArmorStand) getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);

        minion.setBasePlate(false);
        minion.setSmall(true);
        minion.setGravity(false);
        minion.setArms(true);

        minion.setHelmet(getSkull());
        minion.setChestplate(getChestplate());
        minion.setLeggings(getLeggings());
        minion.setBoots(getBoots());
        minion.setItemInHand(getTool());
    }

    /**
     * Stopper en minion fra at køre tasks
     */
    public void disable() {
        this.isDisabled = true;
    }

    public ItemStack getTool() {
        switch (getType()) {
            case ATTACK:
                return new ItemStack(Material.IRON_SWORD);
            case DIG:
                return new ItemStack(Material.IRON_SPADE);
            case PICKUP:
                return new ItemStack(Material.HOPPER);
            default:
                return new ItemStack(Material.BLAZE_ROD);
        }
    }

    /**
     * @return Minionen som et entity
     */
    public ArmorStand getMinion() {
        Location location = new Location(getWorld(), x, y, z);

        Optional<Entity> optional = getWorld().getNearbyEntities(location, 1, 1, 1).stream().filter(x -> x instanceof ArmorStand).findFirst();

        return (ArmorStand) optional.orElse(null);
    }

    public ItemStack getChestplate() {
        return setAverageColor(new ItemStack(Material.LEATHER_CHESTPLATE));
    }
    public ItemStack getLeggings() {
        return setAverageColor(new ItemStack(Material.LEATHER_LEGGINGS));
    }
    public ItemStack getBoots() {
        return setAverageColor(new ItemStack(Material.LEATHER_BOOTS));
    }
    public ItemStack setAverageColor(ItemStack leatherArmor) {
        Color color = getAverageColorFromSkin();
        LeatherArmorMeta meta = (LeatherArmorMeta) leatherArmor.getItemMeta();
        meta.setColor(color);
        leatherArmor.setItemMeta(meta);
        return leatherArmor;
    }

    public ItemStack getSkull() {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(Bukkit.getPlayer(UUID.fromString(uuidOfOwner)).getDisplayName());
        skull.setItemMeta(skullMeta);
        return skull;
    }

    /**
     * Får gennemsnitlig skin farve ved at scanne hver pixel i ejerens skin og dividere det med hvor mange pixels skinnet er
     * @return Gennemsnitlig skin farve
     */
    public Color getAverageColorFromSkin() {
        String skinURL = "https://mineskin.eu/skin/" + uuidOfOwner;
        try {
            URL url = new URL(skinURL);
            BufferedImage image = ImageIO.read(url);

            int width = image.getWidth();
            int height = image.getHeight();
            int totalPixels = 0;
            long redSum = 0;
            long greenSum = 0;
            long blueSum = 0;

            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    if (red == 255 && green == 255 && blue == 255) {
                        continue;
                    }
                    if (red == 0 && green == 0 && blue == 0) {
                        continue;
                    }

                    totalPixels++;
                    redSum += red;
                    greenSum += green;
                    blueSum += blue;
                }
            }
            if(redSum == 0) redSum = 1;
            if(greenSum == 0) greenSum = 1;
            if(blueSum == 0) blueSum = 1;

            int avgRed = (int) (redSum / totalPixels);
            int avgGreen = (int) (greenSum / totalPixels);
            int avgBlue = (int) (blueSum / totalPixels);

            return Color.fromRGB(avgRed, avgGreen, avgBlue);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return Color.fromRGB(0, 0, 0);
    }




    public World getWorld() {
        return Bukkit.getWorld(world);
    }


    public OfflinePlayer getOwner() {
        return Bukkit.getPlayer(UUID.fromString(uuidOfOwner));
    }
    public int getCooldown() {
        return 60 - (Math.min(10, level) * 2);
    }
    public minionType getType() {
        switch (type) {
            case 1:
                return minionType.DIG;
            case 2:
                return minionType.PICKUP;
            case 3:
                return minionType.ATTACK;
            default:
                return minionType.SELL;
        }
    }

}
