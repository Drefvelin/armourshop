package net.tfminecraft.ArmourShop.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.tfminecraft.ArmourShop.ArmourShop;
import net.tfminecraft.ArmourShop.utils.Permissions;


public class CommandManager implements Listener, CommandExecutor {
	public String cmd1 = "armourshop";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase(cmd1) && args.length == 0) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				InventoryManager i = new InventoryManager();
				i.categoryView(player);
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase(cmd1) && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if(Permissions.isAdmin(sender)) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					JavaPlugin.getPlugin(ArmourShop.class).reloadMessage(p);
				} else {
					JavaPlugin.getPlugin(ArmourShop.class).reload();
				}
				return true;
			}
			if(sender instanceof Player) {
				Player p = (Player) sender;
				p.sendMessage("§a[ArmourShop] §cYou do not have access to this command");
			}
		}
		return false;
	}

}
