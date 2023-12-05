package dk.spacemc.minions.classes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dk.spacemc.minions.Minions;
import dk.wavebleak.sell.SellManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


import static dk.spacemc.minions.Minions.getInstance;

public class Minion {
    public static enum minionType {
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

        //getInstance().minions.add(this);
        //getInstance().manager.saveData(getInstance().minions);
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
        if(getMinion() == null) return;
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
        if(getMinion() == null) return;
        switch (frame) {
            case 1:
                // Arm is high up
                minion.setRightArmPose(new EulerAngle(Math.toRadians(215), Math.toRadians(1), Math.toRadians(1)));
                break;
            case 2:
                // Arm is a bit up
                minion.setRightArmPose(new EulerAngle(Math.toRadians(250), Math.toRadians(1), Math.toRadians(1)));
                break;
            case 3:
                // Arm is a bit down
                minion.setRightArmPose(new EulerAngle(Math.toRadians(285), Math.toRadians(1), Math.toRadians(1)));
                break;
            case 4:
                // Arm is far down
                minion.setRightArmPose(new EulerAngle(Math.toRadians(325), Math.toRadians(1), Math.toRadians(1)));
                break;
        }
    }

    /**
     * Denne funktion bliver kaldt hvis det er en DIG minion, hver calcCooldown ticks
     */
    public void dig() {
        ArmorStand minion = getMinion();

        Block targetBlock = minion.getTargetBlock((Set<Material>) null, 3);

        targetBlock.breakNaturally();
    }

    /**
     * Denne funktion bliver kaldt hvis det er en PICKUP minion, hver calcCooldown ticks
     */
    public void pickup() {
        Chest chest = getChest();
        ArmorStand minion = getMinion();

        Location location = minion.getLocation();

        List<Entity> items = getWorld().getNearbyEntities(location, 3, 1, 3).stream().filter(x -> x instanceof Item).collect(Collectors.toList());

        for(Entity itemEntity : items) {
            Item item = (Item) itemEntity;
            ItemStack stack = item.getItemStack();
            if(!hasRoomForItem(stack)) continue;
            chest.getBlockInventory().addItem(stack);
            item.remove();
        }
    }

    /**
     * Denne funktion bliver kaldt hvis det er en ATTACK minion, hver calcCooldown ticks

     */
    public void attack() {
        ArmorStand minion = getMinion();
        List<Entity> entities = minion.getNearbyEntities(3, 3, 3);
        Vector dir = minion.getLocation().getDirection();
        for(Entity entity : entities){
            if(!(entity instanceof LivingEntity)) continue;
            Vector from = minion.getLocation().toVector();
            Vector to = entity.getLocation().toVector();
            Vector fromTo = to.subtract(from);

            if(dir.dot(fromTo) > 0.8) {
                ((LivingEntity) entity).damage(5D); //wher do we do the spawning dodilligence?
            }
        }
    }

    /**
     * Denne funktion bliver kaldt hvis det er en SELL minion, hver calcCooldown ticks
     */
    public void sell() {
        Chest chest = getChest();

        ListIterator<ItemStack> iterator = chest.getBlockInventory().iterator();
        ArrayList<ItemStack> itemsToSell = new ArrayList<>();
        while (iterator.hasNext()) {
            itemsToSell.add(iterator.next());
        }

        double profit = SellManager.sellItems(itemsToSell, getOwner(), chest.getBlockInventory(), false);

        if(profit > 0) {
            new HologramManager("&a+ " + profit + "$", getMinion().getEyeLocation().add(0, 0.5, 0), 1D, false).spawn();

        }

    }

    public boolean hasRoomForItem(ItemStack itemStack) {
        Chest chest = getChest();

        Inventory chestInventory = chest.getInventory();

        // Calculate the available space for the item in the chest inventory
        int availableSpace = 0;
        for (ItemStack slot : chestInventory.getContents()) {
            if (slot == null || slot.getType() == Material.AIR) {
                availableSpace += itemStack.getMaxStackSize();
            } else if (slot.isSimilar(itemStack) && slot.getAmount() < itemStack.getMaxStackSize()) {
                availableSpace += itemStack.getMaxStackSize() - slot.getAmount();
            }
        }
        return availableSpace >= itemStack.getAmount();
    }



    public Chest getChest() {
        Location chestLocation = new Location(getWorld(), chestX, chestY, chestZ);
        return (Chest) getWorld().getBlockAt(chestLocation).getState();
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

    /**
     * Her er prisen for at opgradere sin minion fra det level den er nu, til det level der er over det
     * @return Prisen for at opgradere
     */
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

        minion.setMetadata("invulnerable", new FixedMetadataValue(getInstance(), true));

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

    public boolean isDisabled() {
        return isDisabled;
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

        Optional<Entity> optional = getWorld().getNearbyEntities(location, 5, 5, 5)
                .stream()
                .filter(entity -> entity.getType().equals(EntityType.ARMOR_STAND))
                .min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(location)));

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
        ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) skullItem.getItemMeta();


        GameProfile gameProfile = new GameProfile(java.util.UUID.fromString(uuidOfOwner), null);
        gameProfile.getProperties().put("textures", new Property("textures", getTextureValue()));

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, gameProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        skullItem.setItemMeta(meta);
        return skullItem;
    }

    public String getTextureValue() {
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuidOfOwner;


        try {
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                String jsonResponse = response.toString();

                Gson gson = new Gson();
                JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);

                JsonArray properties = json.getAsJsonArray("properties");
                JsonObject texture = properties.get(0).getAsJsonObject();

                return texture.get("value").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
                    int alpha = (rgb >> 24) & 0xFF;
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    if(alpha == 0) continue;

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

    public static int getType(minionType type) {
        switch(type) {
            case DIG:
                return 1;
            case PICKUP:
                return 2;
            case ATTACK:
                return 3;
            default:
                return 4;
        }
    }

    public int getLevel() {
        return level;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }

    public String getUuidOfOwner() {
        return uuidOfOwner;
    }

    public double getChestX() {
        return chestX;
    }
    public double getChestY() {
        return chestY;
    }
    public double getChestZ() {
        return chestZ;
    }

    public void remove() {
        getMinion().remove();
        disable();
        getInstance().minions.remove(this);
    }

    public static boolean isMinion(Entity entity) {
        if(getInstance().minions == null) {
            getInstance().getLogger().info("MINIONS IS NULL");
            return false;
        }
        Optional<Minion> minion = getInstance().minions.stream().filter(x -> {
            if(x.getMinion() == null) {
                getInstance().getLogger().info("GETMINION IS NULL");
                return false;
            }
            return x.getMinion().equals(entity);
        }).findAny();

        return minion.isPresent();
    }

    public static Minion getMinion(Entity entity) {
        Optional<Minion> minion = getInstance().minions.stream().filter(x -> x.getMinion().equals(entity)).findAny();

        return minion.orElse(null);
    }

}
