package dk.spacemc.minions.commands;

import dk.spacemc.minions.classes.Minion;
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
        if(args.length == 0) {
            int number = Minion.getType(Minion.minionType.DIG);

            Block chest = player.getTargetBlock((Set<Material>) null, 15);

            if (!(chest instanceof Chest)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDer blev ikke fundet en kiste!"));
                return false;
            }

            Minion minion = new Minion(1, number, player.getUniqueId().toString(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), chest.getX(), chest.getY(), chest.getZ(), player.getWorld().getName());
            minion.spawn();
            minion.run();
            return true;
        } else {
            Minion.minionType type;
            try {
                type = Minion.minionType.valueOf(args[0]);
            }catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDer blev ikke fundet den minion type!"));
                return false;
            }

            int number = Minion.getType(type);

            Block chest = player.getTargetBlock((Set<Material>) null, 15);

            if(!(chest instanceof Chest)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDer blev ikke fundet en kiste!"));
                return false;
            }

            Minion minion = new Minion(1, number, player.getUniqueId().toString(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), chest.getX(), chest.getY(), chest.getZ(), player.getWorld().getName());
            minion.spawn();
            minion.run();
            return true;
        }
    }
}
