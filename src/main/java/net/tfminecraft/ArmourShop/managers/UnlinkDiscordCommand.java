package net.tfminecraft.ArmourShop.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.tfminecraft.ArmourShop.ArmourShop;
import net.tfminecraft.ArmourShop.api.ProvinceSystemClient;
import net.tfminecraft.ArmourShop.utils.ChatMessages;

/**
 * /unlinkdiscord — remove durable Discord link for this Minecraft UUID.
 */
public class UnlinkDiscordCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Players only.");
			return true;
		}

		Player player = (Player) sender;
		String uuid = player.getUniqueId().toString();
		ArmourShop plugin = JavaPlugin.getPlugin(ArmourShop.class);

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			ProvinceSystemClient.SimpleResult result = ProvinceSystemClient.unlinkDiscord(uuid);
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!player.isOnline()) {
					return;
				}
				if (!result.ok) {
					ChatMessages.error(
						player,
						result.error != null ? result.error : "Unlink failed."
					);
					return;
				}
				ChatMessages.info(
					player,
					"Discord unlinked. Use " + ChatColor.AQUA + "/linkdiscord"
						+ ChatColor.GRAY + " to link again before uploading skins."
				);
			});
		});
		return true;
	}
}
