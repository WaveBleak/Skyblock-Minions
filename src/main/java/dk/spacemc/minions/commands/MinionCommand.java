package dk.spacemc.minions.commands;

import dk.spacemc.minions.Minions;
import dk.spacemc.minions.classes.Minion;
import dk.spacemc.minions.classes.MinionEgg;
import dk.wavebleak.sell.SellManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class MinionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if(!player.isOp()) return false;

        if(args.length == 0) {
            SellManager.sendPlayerMessage(player, "&cGyldig kommando: /minion [DIG,PICKUP,ATTACK,SELL]");
            return false;
        }
        MinionEgg egg = new MinionEgg(Minion.minionType.valueOf(args[0].toUpperCase()), 1, 0, 0, 0, 0, 0);

        player.getInventory().addItem(egg.getEgg());
        return true;
    }
}
