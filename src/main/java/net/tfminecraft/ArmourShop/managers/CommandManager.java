package net.tfminecraft.ArmourShop.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.tfminecraft.ArmourShop.ArmourShop;
import net.tfminecraft.ArmourShop.api.ProvinceSystemClient;
import net.tfminecraft.ArmourShop.utils.ChatMessages;
import net.tfminecraft.ArmourShop.utils.ExpiryFormat;
import net.tfminecraft.ArmourShop.utils.Permissions;


public class CommandManager implements Listener, CommandExecutor, TabCompleter {
	public String cmd1 = "armourshop";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase(cmd1)) {
			return false;
		}

		if (args.length == 0) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				InventoryManager i = new InventoryManager();
				i.typeView(player);
				return true;
			}
			return false;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (Permissions.isAdmin(sender)) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					JavaPlugin.getPlugin(ArmourShop.class).reloadMessage(p);
				} else {
					JavaPlugin.getPlugin(ArmourShop.class).reload();
				}
				return true;
			}
			if (sender instanceof Player) {
				Player p = (Player) sender;
				p.sendMessage("§a[ArmourShop] §cYou do not have access to this command");
			}
			return true;
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("token")
			&& args[1].equalsIgnoreCase("create")) {
			return handleTokenCreate(sender);
		}

		return false;
	}

	private boolean handleTokenCreate(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Players only.");
			return true;
		}

		Player player = (Player) sender;
		if (!Permissions.canCreateToken(player)) {
			ChatMessages.error(player, "You do not have permission to create a skins token.");
			return true;
		}

		String uuid = player.getUniqueId().toString();
		ArmourShop plugin = JavaPlugin.getPlugin(ArmourShop.class);
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			ProvinceSystemClient.CodeResult result = ProvinceSystemClient.issueSkinsCode(uuid);
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!player.isOnline()) {
					return;
				}
				if (!result.ok) {
					ChatMessages.error(
						player,
						result.error != null ? result.error : "Could not create skins code."
					);
					return;
				}
				ChatMessages.sendCopyableCode(
					player,
					"Your skins upload code (click to copy):",
					result.code
				);
				ChatMessages.info(player, "Redeem on the skins website.");
				String expiry = ExpiryFormat.relativeLabel(result.expiresAt);
				if (expiry != null) {
					ChatMessages.info(player, expiry);
				}
			});
		});
		return true;
	}

	@Override
	public List<String> onTabComplete(
		CommandSender sender,
		Command command,
		String alias,
		String[] args
	) {
		if (!command.getName().equalsIgnoreCase(cmd1)) {
			return Collections.emptyList();
		}

		if (args.length == 1) {
			List<String> completions = new ArrayList<>();
			if (Permissions.canCreateToken(sender)) {
				completions.add("token");
			}
			if (Permissions.isAdmin(sender)) {
				completions.add("reload");
			}
			return filter(completions, args[0]);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("token")
			&& Permissions.canCreateToken(sender)) {
			return filter(Collections.singletonList("create"), args[1]);
		}

		return Collections.emptyList();
	}

	private List<String> filter(List<String> list, String input) {
		String prefix = input == null ? "" : input.toLowerCase();
		return list.stream()
			.filter(s -> s.toLowerCase().startsWith(prefix))
			.collect(Collectors.toList());
	}
}
