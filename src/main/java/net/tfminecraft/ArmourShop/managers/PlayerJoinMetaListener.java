package net.tfminecraft.ArmourShop.managers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.tfminecraft.ArmourShop.entitlements.PlayerMetaSyncService;

/**
 * Sync skin-upload entitlements to ProvinceSystem on join.
 */
public final class PlayerJoinMetaListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event) {
		if (event.getPlayer() == null) {
			return;
		}
		PlayerMetaSyncService.pushForPlayer(event.getPlayer());
	}
}
