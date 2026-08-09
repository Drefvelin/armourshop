package net.tfminecraft.ArmourShop.managers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.tfminecraft.ArmourShop.api.ProvinceSystemClient;
import net.tfminecraft.ArmourShop.utils.ChatMessages;
import net.tfminecraft.ArmourShop.utils.ExpiryFormat;

/**
 * /linkdiscord — start Discord account link (one-time code for Discord /linkdiscord).
 */
public class LinkDiscordCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Players only.");
			return true;
		}

		Player player = (Player) sender;
		ProvinceSystemClient.CodeResult result = ProvinceSystemClient.startDiscordLink(
			player.getUniqueId().toString(),
			player.getName()
		);

		if (!result.ok) {
			ChatMessages.error(player, result.error != null ? result.error : "Link failed.");
			return true;
		}

		if (result.alreadyLinked) {
			String name = result.discordUsername;
			if (name != null && !name.isBlank()) {
				ChatMessages.info(
					player,
					"Already linked with Discord user " + ChatColor.AQUA + name.trim()
				);
			} else {
				ChatMessages.info(player, "Already linked.");
			}
			return true;
		}

		ChatMessages.sendCopyableCode(
			player,
			"Your Discord link code (click to copy):",
			result.code
		);
		ChatMessages.info(
			player,
			"In Discord, run " + ChatColor.AQUA + "/linkdiscord <code>"
				+ ChatColor.GRAY + " with that code."
		);
		String expiry = ExpiryFormat.relativeLabel(result.expiresAt);
		if (expiry != null) {
			ChatMessages.info(player, expiry);
		}
		return true;
	}
}
