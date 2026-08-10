package net.tfminecraft.ArmourShop.pack.catalog;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import net.tfminecraft.ArmourShop.ArmourShop;
import net.tfminecraft.ArmourShop.Cache;
import net.tfminecraft.ArmourShop.api.ProvinceSystemClient;
import net.tfminecraft.ArmourShop.loaders.CategoryLoader;
import net.tfminecraft.ArmourShop.objects.ScrollOption;
import net.tfminecraft.ArmourShop.objects.SkinCategory;
import net.tfminecraft.ArmourShop.objects.SkinSet;

/**
 * Build ArmourShop catalog JSON and push to ProvinceSystem (fail-soft).
 */
public final class CatalogSyncService {

	private CatalogSyncService() {}

	/** Build payload from in-memory categories + Cache.scrolls. */
	public static String buildPayloadJson() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("{\"categories\":[");
		boolean firstCat = true;
		for (SkinCategory cat : CategoryLoader.get()) {
			if (cat == null || cat.getId() == null || cat.getId().isBlank()) {
				continue;
			}
			if (!firstCat) {
				sb.append(',');
			}
			firstCat = false;
			String name = cat.getName() == null ? cat.getId() : cat.getName();
			name = ChatColor.stripColor(name);
			sb.append("{\"id\":\"").append(escape(cat.getId())).append('"');
			sb.append(",\"name\":\"").append(escape(name)).append('"');
			sb.append(",\"is_item\":").append(cat.isItem());
			sb.append(",\"skin_sets\":[");
			boolean firstSet = true;
			List<SkinSet> sets = cat.getSets();
			if (sets != null) {
				for (SkinSet set : sets) {
					if (set == null || set.getId() == null || set.getId().isBlank()) {
						continue;
					}
					if (!firstSet) {
						sb.append(',');
					}
					firstSet = false;
					sb.append('"').append(escape(set.getId())).append('"');
				}
			}
			sb.append("]}");
		}
		sb.append("],\"scrolls\":[");
		boolean firstScroll = true;
		List<ScrollOption> scrolls = Cache.scrolls;
		if (scrolls != null) {
			for (ScrollOption scroll : scrolls) {
				if (scroll == null || scroll.getId().isEmpty()) {
					continue;
				}
				if (!firstScroll) {
					sb.append(',');
				}
				firstScroll = false;
				sb.append("{\"id\":\"").append(escape(scroll.getId())).append('"');
				sb.append(",\"label\":\"").append(escape(scroll.getLabel())).append("\"}");
			}
		}
		sb.append("]}");
		return sb.toString();
	}

	/**
	 * Push on a worker thread. Logs success or warning; never throws to caller.
	 */
	public static void pushAsync(JavaPlugin plugin) {
		if (plugin == null) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			ProvinceSystemClient.CatalogPushResult result = pushNow();
			Logger log = plugin.getLogger();
			if (result.ok) {
				log.info("[catalog] synced to ProvinceSystem: categories="
					+ result.categories
					+ " skin_sets=" + result.skinSets
					+ " scrolls=" + result.scrolls);
			} else {
				log.warning("[catalog] sync failed: " + result.error);
			}
		});
	}

	/** Synchronous push (async task or command feedback). */
	public static ProvinceSystemClient.CatalogPushResult pushNow() {
		return ProvinceSystemClient.pushCatalog(buildPayloadJson());
	}

	/** Convenience from plugin singleton. */
	public static void pushAsyncFromPlugin() {
		ArmourShop plugin = ArmourShop.plugin;
		if (plugin != null) {
			pushAsync(plugin);
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
				case '\n' -> out.append("\\n");
				case '\r' -> out.append("\\r");
				case '\t' -> out.append("\\t");
				default -> {
					if (c < 0x20) {
						out.append(String.format("\\u%04x", (int) c));
					} else {
						out.append(c);
					}
				}
			}
		}
		return out.toString();
	}
}
