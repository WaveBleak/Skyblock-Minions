package dk.spacemc.minions.classes;

import org.bukkit.inventory.Inventory;

public class InventoryData {

     private final InventoryManager lambda;
     private final Inventory inventory;

     public InventoryData(InventoryManager lambda, Inventory inventory) {
         this.lambda = lambda;
         this.inventory = inventory;
     }


    public Inventory getInventory() {
        return inventory;
    }

    public InventoryManager getLambda() {
        return lambda;
    }
}
