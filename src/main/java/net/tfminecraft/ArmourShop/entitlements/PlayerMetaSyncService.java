package net.tfminecraft.ArmourShop.entitlements;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.tfminecraft.ArmourShop.ArmourShop;
import net.tfminecraft.ArmourShop.api.ProvinceSystemClient;

/**
 * Push resolved skin-upload entitlements to ProvinceSystem (fail-soft).
 */
public final class PlayerMetaSyncService {

	private PlayerMetaSyncService() {}

	public static void pushForPlayer(Player player) {
		if (player == null) {
			return;
		}
		pushAsync(player.getUniqueId());
	}

	public static void pushAsync(UUID playerUuid) {
		if (playerUuid == null || ArmourShop.plugin == null) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(
			ArmourShop.plugin,
			() -> pushNow(playerUuid)
		);
	}

	/** Push meta for every online player (e.g. after /armourshop reload). */
	public static void pushAllOnlineAsync() {
		if (ArmourShop.plugin == null) {
			return;
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player != null) {
				pushAsync(player.getUniqueId());
			}
		}
	}

	public static void pushNow(UUID playerUuid) {
		if (playerUuid == null) {
			return;
		}
		Player online = Bukkit.getPlayer(playerUuid);
		if (online == null || !online.isOnline()) {
			return;
		}
		int stops = PermissionGroupService.getNameColourStops(online);
		int pairBytes = PermissionGroupService.getMax3dPairBytes(online);
		int cooldownDays = PermissionGroupService.getSkinTokenCooldownDays(online);
		boolean allowHelmet = PermissionGroupService.getAllowArmor3dHelmet(online);
		List<String> kinds = PermissionGroupService.getSkinKinds(online);
		StringBuilder body = new StringBuilder(256);
		body.append("{\"player_uuid\":\"").append(playerUuid).append('"');
		body.append(",\"name_colour_stops\":").append(stops);
		body.append(",\"max_3d_pair_bytes\":").append(pairBytes);
		body.append(",\"skin_token_cooldown_days\":").append(cooldownDays);
		body.append(",\"allow_armor_3d_helmet\":").append(allowHelmet);
		body.append(",\"skin_kinds\":[");
		boolean first = true;
		for (String kind : kinds) {
			if (kind == null || kind.isBlank()) {
				continue;
			}
			if (!first) {
				body.append(',');
			}
			first = false;
			body.append('"').append(escape(kind.trim().toLowerCase())).append('"');
		}
		body.append("]}");
		ProvinceSystemClient.SimpleResult result =
			ProvinceSystemClient.pushPlayerMeta(body.toString());
		if (!result.ok && ArmourShop.plugin != null) {
			ArmourShop.plugin.getLogger().warning(
				"[player-meta] push failed for " + playerUuid + ": " + result.error
			);
		}
	}

	private static String escape(String raw) {
		if (raw == null) {
			return "";
		}
		StringBuilder out = new StringBuilder(raw.length() + 8);
		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);
			switch (c) {
				case '\\' -> out.append("\\\\");
				case '"' -> out.append("\\\"");
				default -> out.append(c);
			}
		}
		return out.toString();
	}
}
