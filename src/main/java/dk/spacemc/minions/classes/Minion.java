package dk.spacemc.minions.classes;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dk.spacemc.minions.Minions;
import dk.wavebleak.sell.SellManager;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.*;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
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


import static dk.spacemc.minions.Minions.api;
import static dk.spacemc.minions.Minions.getInstance;

/**
 * The Minion class represents a minion with various attributes and actions.
 */
public class Minion {
    public int getItemsSold() {
        return itemsSold;
    }

    public int getItemsPickedUp() {
        return itemsPickedUp;
    }

    public int getSecondsAlive() {
        return secondsAlive;
    }

    public double getYaw() {
        return yaw;
    }

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
    private double chestX;
    private double chestY;
    private double chestZ;
    private final double yaw;
    private final String world;
    private int blocksBroken;
    private int entitiesKilled;
    private int itemsSold;
    private int itemsPickedUp;
    private int secondsAlive;
    private boolean isRunning;
    private Hologram hologram;


    /**
     * Constructor for minions
     * @param level Minionens level
     * @param type Minionens type
     * @param owner Ejeren
     * @param spawnLoc Lokation af spawn
     * @param chestLoc Lokation af chest
     * @param yaw Direktion af hvor minionen skal kikke
     */
    public Minion(int level, minionType type, Player owner, Location spawnLoc, Location chestLoc, double yaw) {
        this(level, getType(type), owner.getUniqueId().toString(), spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), chestLoc.getBlockX(), chestLoc.getBlockY(), chestLoc.getBlockZ(), yaw, spawnLoc.getWorld().getName());
    }


    /**
     * Constructor for Minion.
     * @param level The level of the Minion.
     * @param type The type of the Minion.
     * @param uuidOfOwner The UUID of the owner of the Minion.
     * @param x The x-coordinate of the Minion's spawn location.
     * @param y The y-coordinate of the Minion's spawn location.
     * @param z The z-coordinate of the Minion's spawn location.
     * @param chestX The x-coordinate of the Minion's chest location.
     * @param chestY The y-coordinate of the Minion's chest location.
     * @param chestZ The z-coordinate of the Minion's chest location.
     * @param yaw The yaw/direction of the Minion.
     * @param world The name of the world the Minion is in.
     */
    public Minion(int level, int type, String uuidOfOwner, double x, double y, double z, double chestX, double chestY, double chestZ, double yaw, String world) {
        this.level = level;
        this.type = type;
        this.uuidOfOwner = uuidOfOwner;
        this.x = x;
        this.y = y;
        this.z = z;
        this.chestX = chestX;
        this.chestY = chestY;
        this.chestZ = chestZ;
        this.yaw = yaw;
        this.world = world;
        this.itemsSold = 0;
        this.itemsPickedUp = 0;
        this.entitiesKilled = 0;
        this.blocksBroken = 0;
        this.secondsAlive = 0;
        this.isRunning = false;
        this.hologram = null;

        //getInstance().minions.add(this);
        //getInstance().manager.saveData(getInstance().minions);
    }

    public void run() {
        run(getInstance());
    }

    public void run(JavaPlugin plugin) {
        if(taskManager != null) {
            taskManager.cancel(); // Undg\u00E5 flere tasks at k\u00F8re p\u00E5 samme tid
        }
        if(animationManager != null) {
            animationManager.cancel();
        }
        if(secondsAliveCounter != null) {
            secondsAliveCounter.cancel();
        }

        secondsAliveCounter = new BukkitRunnable() {
            @Override
            public void run() {
                secondsAlive++;
            }
        }.runTaskTimer(plugin, 20, 20);

        taskManager = new BukkitRunnable() {
            @Override
            public void run() {
                isRunning = true;
                performAction();
            }
        }.runTaskTimer(plugin, getCooldown(), getCooldown());


        animationManager = new BukkitRunnable() {
            private int frames = 1;
            @Override
            public void run() {
                if(frames > 4) frames = 1;
                performAnimation(frames);
                frames++;

            }
        }.runTaskTimer(plugin, getCooldown() / 4, getCooldown() / 4);
    }

    private BukkitTask taskManager = null;
    private BukkitTask animationManager = null;
    private BukkitTask secondsAliveCounter = null;


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
     * S\u00E6t armen p\u00E5 minionen i en s\u00E6rlig position
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

        Set<Material> set = new HashSet<>();

        set.add(Material.AIR);
        set.add(Material.WATER);
        set.add(Material.LAVA);
        set.add(Material.STATIONARY_LAVA);
        set.add(Material.STATIONARY_WATER);
        set.add(Material.BARRIER);
        set.add(Material.BEDROCK);
        set.add(Material.COMMAND);

        Block targetBlock = minion.getTargetBlock(set, 3);

        if(targetBlock == null) {
            return;
        }

        if(targetBlock.breakNaturally()) {
            this.blocksBroken++;
        }

    }

    /**
     * Denne funktion bliver kaldt hvis det er en PICKUP minion, hver calcCooldown ticks
     */
    public void pickup() {
        Chest chest = getChest();

        if(chest == null) return;

        Inventory inventory = chest.getBlockInventory();
        if(chest.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();

            inventory = doubleChest.getInventory();
        }

        ArmorStand minion = getMinion();

        Location location = minion.getLocation();

        List<Entity> items = getWorld().getNearbyEntities(location, 3, 1, 3).stream().filter(x -> x instanceof Item).collect(Collectors.toList());

        Optional<Entity> optional = items.stream().findFirst();

        if(optional.isPresent()) {
            Item item = (Item) optional.get();
            StackedItem stackedItem = WildStackerAPI.getStackedItem(item);

            if(!hasRoomForItem(stackedItem.getItemStack())) {
                ItemStack tester = stackedItem.getItemStack();
                tester.setAmount(64);
                if(hasRoomForItem(tester)) {
                    stackedItem.setStackAmount(stackedItem.getStackAmount() - 64, true);
                    inventory.addItem(tester);
                }
                return;
            }
            stackedItem.giveItemStack(inventory);
            item.remove();
            this.itemsPickedUp++;
        }
    }

    /**
     * Denne funktion bliver kaldt hvis det er en ATTACK minion, hver calcCooldown ticks
     */
    public void attack() {
        ArmorStand minion = getMinion();
        List<Entity> entities = minion.getNearbyEntities(3, 3, 3);
        Vector dir = minion.getLocation().getDirection();
        boolean hasAttackedOnce = false;
        for(Entity entity : entities){
            if(hasAttackedOnce) continue;
            if(!(entity instanceof LivingEntity)) continue;
            Vector from = minion.getLocation().toVector();
            Vector to = entity.getLocation().toVector();
            Vector fromTo = to.subtract(from);

            if(dir.dot(fromTo) > 0.8) {
                hasAttackedOnce = true;
                double health = ((LivingEntity) entity).getHealth();
                if(health - 50 <= 0) {
                    this.entitiesKilled++;
                }
                ((LivingEntity) entity).damage(5D);
            }
        }
    }

    /**
     * Denne funktion bliver kaldt hvis det er en SELL minion, hver calcCooldown ticks
     */
    public void sell() {
        Chest chest = getChest();

        if(chest == null) return;


        ArrayList<ItemStack> items = new ArrayList<>();


        Inventory inventoryToSell = chest.getBlockInventory();
        if(chest.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();

            inventoryToSell = doubleChest.getInventory();
        }

        for(ItemStack item : inventoryToSell.getContents()) {
            if(item == null) continue;
            if(!item.getType().equals(Material.AIR) && SellManager.canBeSold(item)) {
                items.add(item);
                break;
            }
        }


        double profit = SellManager.sellItems(items, getOwner(), inventoryToSell, false);
        int itemsSold = 0;
        for(ItemStack item : items) {
            itemsSold += item.getAmount();
        }

        this.itemsSold += itemsSold;

        if(profit > 0) {
            new HologramManager("&a+ " + profit + "$", getMinion().getEyeLocation().add(0, 1.75, 0), 1D).spawn();

        }

    }

    public boolean hasAnyItemAtAll(Inventory inv) {
        return inv.getContents().length != 0;
    }

    public boolean hasRoomForItem(ItemStack itemStack) {
        Chest chest = getChest();

        if(chest == null) return false;

        Inventory chestInventory = chest.getBlockInventory();

        if(chest.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();

            chestInventory = doubleChest.getInventory();
        }

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
        Location chestLocation = getChestLocation();
        if(!chestLocation.getWorld().getBlockAt(chestLocation).getType().equals(Material.CHEST)) return null;
        return (Chest) getWorld().getBlockAt(chestLocation).getState();
    }

    public boolean isSpawned() {
        return (getMinion() != null);
    }

    /**
     * Upgrades the minion if the level is below 20 and the owner has enough balance.
     *
     * @return true if the upgrade was successful, false otherwise.
     */
    public boolean upgrade() {
        if(level >= 20) return false;
        if(getInstance().economy.getBalance(Bukkit.getOfflinePlayer(UUID.fromString(uuidOfOwner))) >= calcUpgradeCost()) {
            getInstance().economy.withdrawPlayer(getOwner(), calcUpgradeCost());
            forceUpgrade();
            return true;
        }
        return false;
    }
    /**
     * Upgrades the minion regardless of its current level. If the optional silent parameter is set to true, a firework is
     * launched to indicate the upgrade. Otherwise, no visual effect is displayed.
     *
     * @param silent If true, no visual effect is displayed. If false, a firework is launched to indicate the upgrade.
     */
    public void forceUpgrade(boolean silent) {
        level++;
        updateHologram();
        run();
        if(!silent) {
            new InstantFirework(FireworkEffect.builder()
                    .trail(true)
                    .withColor(getTop3ColorsFromSkin())
                    .build(),
                    getMinion().getLocation()
            );
        }
    }
    public void forceUpgrade() {
        forceUpgrade(false);
    }

    /**
     * Her er prisen for at opgradere sin minion fra det level den er nu, til det level der er over det
     * @return Prisen for at opgradere
     */
    public int calcUpgradeCost() {
        return (int) Math.round(level * 5.5);
    }


    /**
     * Spawns a minion at the specified location.
     * Sets the properties of the minion such as basePlate, small, gravity, arms, helmet, chestplate, leggings, boots, and itemInHand.
     * Creates a hologram above the minion with the owner's name and minion's type and level.
     */
    public void spawn() {
        Location spawnLocation = new Location(getWorld(), x, y, z);
        spawnLocation.setYaw(Float.parseFloat(yaw + "")); // Spaghetti code
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

        Location hologramLocation = getMinion().getEyeLocation().clone();
        hologramLocation.add(new Vector(0, 0.75, 0));
        String name;
        switch (getType()) {
            case SELL:
                name = "SELL";
                break;
            case ATTACK:
                name = "ATTACK";
                break;
            case DIG:
                name = "DIG";
                break;
            default:
                name = "PICKUP";
                break;
        }

        hologram = api.createHologram(hologramLocation);
        hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', "&7&l" + getOwner().getName() + "'s Minion"));
        hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', "&8&l[ &f" + name + " lvl. " + level + " &8&l]"));


    }


    public void updateHologram() {
        if(hologram == null) return;

        String name;
        switch (getType()) {
            case SELL:
                name = "SELL";
                break;
            case ATTACK:
                name = "ATTACK";
                break;
            case DIG:
                name = "DIG";
                break;
            default:
                name = "PICKUP";
                break;
        }
        hologram.getLines().clear();
        hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', "&7&l" + getOwner().getName() + "'s Minion"));
        hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', "&8&l[ &f" + name + " lvl. " + level + " &8&l]"));
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
        Color color = getTop3ColorsFromSkin()[0];
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

    /**
     * Retrieves the texture value from the Minecraft session server for a given Minion's owner.
     * The texture value is used to create a custom skull item with the owner's skin texture.
     * The texture value is obtained by making a GET request to the Minecraft session server API.
     *
     * @return The texture value as a string, or null if there was an error retrieving the value.
     */
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


    public Color[] getTop3ColorsFromSkin() {
        String skinURL = "https://mineskin.eu/skin/" + uuidOfOwner;
        Map<Integer, Integer> colorFrequency = new HashMap<>();
        try {
            URL url = new URL(skinURL);
            BufferedImage image = ImageIO.read(url);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    colorFrequency.put(rgb, colorFrequency.getOrDefault(rgb, 0) + 1);
                }
            }

            PriorityQueue<Map.Entry<Integer, Integer>> topColors =
                    new PriorityQueue<Map.Entry<Integer, Integer>>(3, Comparator.comparing(Map.Entry::getValue));

            for (Map.Entry<Integer, Integer> entry : colorFrequency.entrySet()) {
                topColors.offer(entry);
                while (topColors.size() > 3) {
                    topColors.poll();
                }
            }

            int index = 2;
            Color[] topThreeColors = new Color[3];
            while (!topColors.isEmpty()) {
                int rgb = topColors.poll().getKey();
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                topThreeColors[index] = Color.fromRGB(red, green, blue);
                index--;
            }
            return topThreeColors;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Color[]{Color.fromRGB(0, 0, 0), Color.fromRGB(0, 0, 0), Color.fromRGB(0, 0, 0)};
    }




    public World getWorld() {
        return Bukkit.getWorld(world);
    }


    public OfflinePlayer getOwner() {
        return Bukkit.getPlayer(UUID.fromString(uuidOfOwner));
    }
    public int getCooldown() {
        // Ensure level is within the valid range (0 to 20)
        int validLevel = Math.max(0, Math.min(level, 20));

        return 60 - validLevel * 2;
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

    public boolean isRunning() {
        return this.isRunning;
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

    public void remove(boolean permanent) {
        if(getMinion() != null) {
            getMinion().remove();
        }
        if(hologram != null) {
            hologram.delete();
        }
        isRunning = false;
        if(permanent)getInstance().minions.remove(this);

    }

    public Location getChestLocation() {
        return new Location(getWorld(), chestX, chestY, chestZ);
    }

    public static boolean isMinion(Entity entity) {
        if(getInstance().minions == null) {
            return false;
        }
        Optional<Minion> minion = getInstance().minions.stream().filter(x -> {
            if(x.getMinion() == null) {
                return false;
            }
            return x.getMinion().equals(entity);
        }).findAny();

        return minion.isPresent();
    }

    public static Minion getMinion(Entity entity) {
        if(getInstance().minions == null) {
            return null;
        }
        Optional<Minion> minion = getInstance().minions.stream().filter(x -> {
            if(x.getMinion() == null) {
                return false;
            }
            return x.getMinion().equals(entity);
        }).findAny();

        return minion.orElse(null);
    }

    public void setChestLoc(Location in) {
        this.chestX = in.getBlockX();
        this.chestY = in.getBlockY();
        this.chestZ = in.getBlockZ();
    }


    public int getBlocksBroken() {
        return this.blocksBroken;
    }
    public void addBlocksBroken(int margin) {
        this.blocksBroken += margin;
    }

    public int getEntitiesKilled() {
        return this.entitiesKilled;
    }
    public void addEntitiesKilled(int margin) {
        this.entitiesKilled += margin;
    }
    public void addItemsSold(int margin) {
        this.itemsSold += margin;
    }
    public void addItemsPickupUp(int margin) {
        this.itemsPickedUp += margin;
    }
    public void addSecondsAlive(int margin) {
        this.secondsAlive += margin;
    }

}
