package net.tfminecraft.ArmourShop.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.tfminecraft.ArmourShop.api.ProvinceSystemClient;
import net.tfminecraft.ArmourShop.api.ProvinceSystemClient.PluginNotice;
import net.tfminecraft.ArmourShop.api.ProvinceSystemClient.PluginNoticesResult;
import net.tfminecraft.ArmourShop.api.ProvinceSystemClient.SimpleResult;
import net.tfminecraft.ArmourShop.utils.ChatMessages;

/**
 * Polls ProvinceSystem for in-game notices (e.g. Discord link success) once per second.
 */
public final class PluginNoticePoller {

	private final JavaPlugin plugin;
	private BukkitTask task;

	public PluginNoticePoller(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void start() {
		if (task != null) {
			return;
		}
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::tick, 20L, 20L);
	}

	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	private void tick() {
		PluginNoticesResult result;
		try {
			result = ProvinceSystemClient.listPluginNotices();
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING, "[notices] poll failed", e);
			return;
		}
		if (!result.ok) {
			plugin.getLogger().fine("[notices] " + result.error);
			return;
		}
		if (result.notices.isEmpty()) {
			return;
		}

		final List<PluginNotice> snapshot = new ArrayList<>(result.notices);
		Bukkit.getScheduler().runTask(plugin, () -> deliverAndAck(snapshot));
	}

	private void deliverAndAck(List<PluginNotice> notices) {
		List<Integer> delivered = new ArrayList<>();

		for (PluginNotice notice : notices) {
			if (notice.type != null && !notice.type.equals("link_success")) {
				continue;
			}
			UUID uuid;
			try {
				uuid = UUID.fromString(notice.playerUuid);
			} catch (IllegalArgumentException e) {
				continue;
			}
			Player player = Bukkit.getPlayer(uuid);
			if (player == null || !player.isOnline()) {
				continue;
			}
			String name = notice.discordUsername;
			if (name != null && !name.isBlank()) {
				ChatMessages.info(
					player,
					"Discord linked successfully with "
						+ ChatColor.AQUA + name.trim()
				);
			} else {
				ChatMessages.info(player, "Discord linked successfully.");
			}
			delivered.add(Integer.valueOf(notice.id));
		}

		if (delivered.isEmpty()) {
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			SimpleResult ack = ProvinceSystemClient.ackPluginNotices(delivered);
			if (!ack.ok) {
				plugin.getLogger().warning("[notices] ack failed: " + ack.error);
			}
		});
	}
}
