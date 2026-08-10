package net.tfminecraft.ArmourShop.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
import net.tfminecraft.ArmourShop.pack.apply.PackPullRunner;
import net.tfminecraft.ArmourShop.pack.catalog.CatalogSyncService;
import net.tfminecraft.ArmourShop.utils.ChatMessages;
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
				p.sendMessage("\u00A7a[ArmourShop] \u00A7cYou do not have access to this command");
			}
			return true;
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("token")
			&& args[1].equalsIgnoreCase("create")) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.YELLOW + "Use "
				+ ChatColor.AQUA + "/token create skin"
				+ ChatColor.YELLOW + " (TFMCWeb) instead.");
			return true;
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("token")
			&& args[1].equalsIgnoreCase("delete")) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "Usage: /armourshop token delete <code>");
			return true;
		}

		if (args.length >= 3
			&& args[0].equalsIgnoreCase("token")
			&& args[1].equalsIgnoreCase("delete")) {
			return handleTokenDelete(sender, args[2]);
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("listtokens")) {
			return handleListTokens(sender);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("pack")
			&& args[1].equalsIgnoreCase("pull")) {
			return handlePackPull(sender);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("catalog")
			&& args[1].equalsIgnoreCase("sync")) {
			return handleCatalogSync(sender);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("submission")
			&& args[1].equalsIgnoreCase("delete")) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "Usage: /armourshop submission delete <id>");
			return true;
		}

		if (args.length >= 3
			&& args[0].equalsIgnoreCase("submission")
			&& args[1].equalsIgnoreCase("delete")) {
			return handleSubmissionDelete(sender, args[2]);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("skin")
			&& args[1].equalsIgnoreCase("delete")) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "Usage: /armourshop skin delete <id>");
			return true;
		}

		if (args.length >= 3
			&& args[0].equalsIgnoreCase("skin")
			&& args[1].equalsIgnoreCase("delete")) {
			return handleSkinDelete(sender, args[2]);
		}

		return false;
	}

	private boolean handleSubmissionDelete(CommandSender sender, String submissionId) {
		if (!Permissions.isAdmin(sender)) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "You do not have access to this command");
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
			+ ChatColor.YELLOW + "Deleting submission…");
		JavaPlugin plugin = JavaPlugin.getPlugin(ArmourShop.class);
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String result = net.tfminecraft.ArmourShop.pack.delete.SubmissionDeleteRunner
				.run(submissionId);
			net.tfminecraft.ArmourShop.pack.delete.DeletableSubmissionCache.invalidate();
			Bukkit.getScheduler().runTask(plugin, () ->
				sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
					+ ChatColor.YELLOW + result)
			);
		});
		return true;
	}

	private boolean handleSkinDelete(CommandSender sender, String skinId) {
		if (!Permissions.isAdmin(sender)) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "You do not have access to this command");
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
			+ ChatColor.YELLOW + "Deleting staff skin…");
		JavaPlugin plugin = JavaPlugin.getPlugin(ArmourShop.class);
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String result = net.tfminecraft.ArmourShop.pack.delete.SkinDeleteRunner
				.run(skinId);
			net.tfminecraft.ArmourShop.pack.delete.DeletableStaffSkinCache.invalidate();
			Bukkit.getScheduler().runTask(plugin, () ->
				sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
					+ ChatColor.YELLOW + result)
			);
		});
		return true;
	}

	private boolean handlePackPull(CommandSender sender) {
		if (!Permissions.isAdmin(sender)) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "You do not have access to this command");
			return true;
		}

		sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
			+ ChatColor.YELLOW + "Pulling approved skins…");
		boolean started = PackPullRunner.run(true, result -> {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.YELLOW + result.summaryLine());
			if (result.busy || result.failed) {
				return;
			}
			int shown = 0;
			for (String line : result.messages) {
				if (shown >= 8) {
					sender.sendMessage(ChatColor.GRAY + "… "
						+ (result.messages.size() - shown)
						+ " more (see console)");
					break;
				}
				sender.sendMessage(ChatColor.GRAY + line);
				shown++;
			}
		});
		if (!started) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.YELLOW + "Pack pull already running.");
		}
		return true;
	}

	private boolean handleCatalogSync(CommandSender sender) {
		if (!Permissions.isAdmin(sender)) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "You do not have access to this command");
			return true;
		}
		ArmourShop plugin = JavaPlugin.getPlugin(ArmourShop.class);
		sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
			+ ChatColor.YELLOW + "Syncing catalog to ProvinceSystem…");
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			ProvinceSystemClient.CatalogPushResult result = CatalogSyncService.pushNow();
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (result.ok) {
					sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
						+ ChatColor.YELLOW + "Catalog synced: categories="
						+ result.categories
						+ " skin_sets=" + result.skinSets
						+ " scrolls=" + result.scrolls);
				} else {
					sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
						+ ChatColor.RED + "Catalog sync failed: " + result.error);
				}
			});
		});
		return true;
	}

	private boolean handleListTokens(CommandSender sender) {
		if (!Permissions.isAdmin(sender)) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "You do not have access to this command");
			return true;
		}

		ArmourShop plugin = JavaPlugin.getPlugin(ArmourShop.class);
		sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
			+ ChatColor.YELLOW + "Fetching active tokens…");
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			ProvinceSystemClient.ActiveCodesResult result =
				ProvinceSystemClient.listActiveCodes();
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!result.ok) {
					sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
						+ ChatColor.RED
						+ (result.error != null ? result.error : "Could not list tokens."));
					return;
				}
				if (result.codes.isEmpty()) {
					sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
						+ ChatColor.YELLOW + "No active unused tokens.");
					return;
				}
				sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
					+ ChatColor.YELLOW + "Active tokens (" + result.codes.size() + "):");
				for (ProvinceSystemClient.ActiveCode entry : result.codes) {
					String owner = ownerLabel(entry);
					if (sender instanceof Player) {
						ChatMessages.sendTokenListLine((Player) sender, entry.code, owner);
					} else {
						sender.sendMessage(ChatColor.AQUA + entry.code
							+ ChatColor.GRAY + " — "
							+ ChatColor.YELLOW + owner);
					}
				}
			});
		});
		return true;
	}

	private boolean handleTokenDelete(CommandSender sender, String codeArg) {
		if (!Permissions.isAdmin(sender)) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "You do not have access to this command");
			return true;
		}

		String code = codeArg == null ? "" : codeArg.trim();
		if (code.startsWith("\"") && code.endsWith("\"") && code.length() >= 2) {
			code = code.substring(1, code.length() - 1).trim();
		}
		if (code.isEmpty()) {
			sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
				+ ChatColor.RED + "Usage: /armourshop token delete <code>");
			return true;
		}

		ArmourShop plugin = JavaPlugin.getPlugin(ArmourShop.class);
		final String toRevoke = code;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			ProvinceSystemClient.SimpleResult result =
				ProvinceSystemClient.revokeCode(toRevoke);
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!result.ok) {
					sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
						+ ChatColor.RED
						+ (result.error != null ? result.error : "Could not delete token."));
					return;
				}
				sender.sendMessage(ChatColor.GREEN + "[ArmourShop] "
					+ ChatColor.YELLOW + "Deleted token "
					+ ChatColor.AQUA + toRevoke);
			});
		});
		return true;
	}

	private static String ownerLabel(ProvinceSystemClient.ActiveCode entry) {
		if (entry.minecraftName != null && !entry.minecraftName.isBlank()) {
			return entry.minecraftName.trim();
		}
		String uuid = entry.playerUuid == null ? "" : entry.playerUuid.trim();
		if (uuid.isEmpty()) {
			return "?";
		}
		try {
			String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
			if (name != null && !name.isBlank()) {
				return name;
			}
		} catch (IllegalArgumentException ignored) {
			// fall through
		}
		return uuid;
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
			if (Permissions.isAdmin(sender)) {
				completions.add("token");
				completions.add("reload");
				completions.add("pack");
				completions.add("catalog");
				completions.add("listtokens");
				completions.add("submission");
				completions.add("skin");
			}
			return filter(completions, args[0]);
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("token")) {
			List<String> completions = new ArrayList<>();
			if (Permissions.isAdmin(sender)) {
				completions.add("delete");
			}
			return filter(completions, args[1]);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("pack")
			&& Permissions.isAdmin(sender)) {
			return filter(List.of("pull"), args[1]);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("catalog")
			&& Permissions.isAdmin(sender)) {
			return filter(List.of("sync"), args[1]);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("submission")
			&& Permissions.isAdmin(sender)) {
			return filter(Collections.singletonList("delete"), args[1]);
		}

		if (args.length == 2
			&& args[0].equalsIgnoreCase("skin")
			&& Permissions.isAdmin(sender)) {
			return filter(Collections.singletonList("delete"), args[1]);
		}

		if (args.length == 3
			&& args[0].equalsIgnoreCase("submission")
			&& args[1].equalsIgnoreCase("delete")
			&& Permissions.isAdmin(sender)) {
			List<String> ids = new ArrayList<>(
				net.tfminecraft.ArmourShop.pack.delete.DeletableSubmissionCache.snapshot()
			);
			return filter(ids, args[2]);
		}

		if (args.length == 3
			&& args[0].equalsIgnoreCase("skin")
			&& args[1].equalsIgnoreCase("delete")
			&& Permissions.isAdmin(sender)) {
			List<String> ids = new ArrayList<>(
				net.tfminecraft.ArmourShop.pack.delete.DeletableStaffSkinCache.snapshot()
			);
			return filter(ids, args[2]);
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
