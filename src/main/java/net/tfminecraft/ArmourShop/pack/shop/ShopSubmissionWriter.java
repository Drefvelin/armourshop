package net.tfminecraft.ArmourShop.pack.shop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.tfminecraft.ArmourShop.Cache;
import net.tfminecraft.ArmourShop.api.ProvinceSystemClient.ApprovedSubmission;

/**
 * Ensures ps_armor / ps_items shop categories and upserts SkinSets after pack write.
 */
public final class ShopSubmissionWriter {

	private ShopSubmissionWriter() {}

	public static void write(ApprovedSubmission sub, Logger log) throws IOException {
		if (sub == null) {
			throw new IllegalArgumentException("submission is null");
		}
		String categoriesDir = Cache.categoriesPath;
		if (categoriesDir == null || categoriesDir.isBlank()) {
			throw new IllegalStateException("pack-apply.categories-path is not set in config.yml");
		}

		Path categoriesPath = Path.of(categoriesDir.trim());
		Files.createDirectories(categoriesPath);

		File categoriesYml = categoriesPath.getParent().resolve("categories.yml").toFile();
		ensureCategoriesIndex(categoriesYml);

		File psArmor = categoriesPath.resolve("ps_armor.yml").toFile();
		File psItems = categoriesPath.resolve("ps_items.yml").toFile();
		ensureCategoryFile(psArmor);
		ensureCategoryFile(psItems);

		String kind = sub.kind == null ? "" : sub.kind.trim().toLowerCase(Locale.ROOT);
		String slug = require(sub.slug, "slug");
		String display = sub.displayName == null || sub.displayName.isBlank()
			? slug
			: sub.displayName.trim();
		// Shared across all tiers of a submission: one LP grant on the submission id/slug.
		String permission = "armourshop.submission." + slug;
		List<String> colours = sub.nameColours == null ? List.of() : sub.nameColours;
		List<String> styles = sub.nameStyles == null ? List.of() : sub.nameStyles;
		boolean addName = sub.addName;

		if ("armor_set".equals(kind)) {
			List<String> tiers = sub.tiers != null && !sub.tiers.isEmpty()
				? sub.tiers
				: (sub.baseSet != null && !sub.baseSet.isBlank()
					? List.of(sub.baseSet.trim())
					: List.of());
			if (tiers.isEmpty()) {
				throw new IllegalStateException(
					"missing tiers (and base_set fallback) for armor submission"
				);
			}
			for (String tier : tiers) {
				String rootKey = slug + "_" + tier;
				String tierDisplay = sub.displayNameForTier(tier);
				upsertArmor(psArmor, rootKey, tierDisplay, tier, permission, colours, styles, addName);
			}
			if (log != null) {
				log.info("[shop] upserted armor_set slug=" + slug + " tiers=" + tiers
					+ " add-name=" + addName);
			}
			return;
		} else if ("handheld".equals(kind)
			|| "large_handheld".equals(kind)
			|| "bow".equals(kind)
			|| "large_bow".equals(kind)
			|| "crossbow".equals(kind)
			|| "item_3d".equals(kind)
			|| "shield".equals(kind)
			|| "helmet_3d".equals(kind)
			|| "gun".equals(kind)) {
			String baseSet = require(sub.baseSet, "base_set");
			upsertItem(psItems, slug, display, baseSet, permission, colours, styles, addName, kind);
		} else {
			throw new IllegalStateException("unsupported shop kind: " + kind);
		}

		if (log != null) {
			log.info("[shop] upserted " + kind + " slug=" + slug + " add-name=" + addName);
		}
	}

	/**
	 * Remove SkinSet key(s) for a submission. For armor, removes each per-tier key
	 * ({@code slug_tier}) from ps_armor plus the bare {@code slug} key (legacy single-pack
	 * submissions). For items, removes the bare {@code slug} key from ps_items.
	 */
	public static void remove(String slug, String kind, List<String> tiers, Logger log)
		throws IOException
	{
		String categoriesDir = Cache.categoriesPath;
		if (categoriesDir == null || categoriesDir.isBlank()) {
			throw new IllegalStateException("pack-apply.categories-path is not set in config.yml");
		}
		String key = require(slug, "slug");
		Path categoriesPath = Path.of(categoriesDir.trim());
		File psArmor = categoriesPath.resolve("ps_armor.yml").toFile();
		File psItems = categoriesPath.resolve("ps_items.yml").toFile();
		boolean changed = false;
		String k = kind == null ? "" : kind.trim().toLowerCase(Locale.ROOT);

		if ("armor_set".equals(k)) {
			if (psArmor.exists()) {
				List<String> tierList = tiers == null ? List.of() : tiers;
				for (String tier : tierList) {
					if (tier == null || tier.isBlank()) {
						continue;
					}
					changed |= clearRoot(psArmor, key + "_" + tier.trim());
				}
				// legacy: submissions written before per-tier packs used the bare slug
				changed |= clearRoot(psArmor, key);
			}
		} else if (psItems.exists()) {
			changed |= clearRoot(psItems, key);
		}

		if (log != null) {
			log.info("[shop] removed SkinSet key(s) slug=" + key + " kind=" + k
				+ " changed=" + changed);
		}
	}

	/** Legacy overload: remove SkinSet key from both ps_armor and ps_items (no-op if missing). */
	public static void remove(String slug, Logger log) throws IOException {
		String categoriesDir = Cache.categoriesPath;
		if (categoriesDir == null || categoriesDir.isBlank()) {
			throw new IllegalStateException("pack-apply.categories-path is not set in config.yml");
		}
		String key = require(slug, "slug");
		Path categoriesPath = Path.of(categoriesDir.trim());
		File psArmor = categoriesPath.resolve("ps_armor.yml").toFile();
		File psItems = categoriesPath.resolve("ps_items.yml").toFile();
		boolean changed = false;
		if (psArmor.exists()) {
			changed |= clearRoot(psArmor, key);
		}
		if (psItems.exists()) {
			changed |= clearRoot(psItems, key);
		}
		if (log != null) {
			log.info("[shop] removed SkinSet key=" + key + " changed=" + changed);
		}
	}

	private static boolean clearRoot(File file, String root) throws IOException {
		FileConfiguration config = loadOrEmpty(file);
		if (!config.contains(root)) {
			return false;
		}
		config.set(root, null);
		config.save(file);
		return true;
	}

	private static void ensureCategoriesIndex(File categoriesYml) throws IOException {
		FileConfiguration config = loadOrEmpty(categoriesYml);
		boolean changed = false;

		if (!config.contains("ps_armor")) {
			config.set("ps_armor.name", "Player Armor");
			config.set("ps_armor.colour", "#a0a0a0");
			config.set("ps_armor.item", "ia.tfmc_armor:decorated_iron_chestplate");
			changed = true;
		}
		if (!config.contains("ps_items")) {
			config.set("ps_items.name", "Player Items");
			config.set("ps_items.colour", "#a0a0a0");
			config.set("ps_items.item", "m.sword.iron_sword");
			config.set("ps_items.is-item", true);
			changed = true;
		}

		if (changed || !categoriesYml.exists()) {
			config.save(categoriesYml);
		}
	}

	private static void ensureCategoryFile(File file) throws IOException {
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			new YamlConfiguration().save(file);
		}
	}

	/**
	 * Upserts one tier's armor SkinSet. {@code rootKey} is {@code {slug}_{tier}} and is
	 * also the pack slug used for the IA piece ids (matches the per-tier pack write).
	 */
	private static void upsertArmor(
		File file,
		String rootKey,
		String display,
		String tier,
		String permission,
		List<String> colours,
		List<String> styles,
		boolean addName
	) throws IOException {
		FileConfiguration config = loadOrEmpty(file);
		String root = rootKey;
		config.set(root + ".name", display);
		writeColour(config, root, colours);
		writeStyles(config, root, styles);
		config.set(root + ".add-name", addName);
		config.set(root + ".set", tier);
		config.set(root + ".permission", permission);
		config.set(root + ".scroll", null);
		config.set(root + ".helmet", "ia.tfmc_submissions:" + rootKey + "_helmet");
		config.set(root + ".chestplate", "ia.tfmc_submissions:" + rootKey + "_chestplate");
		config.set(root + ".leggings", "ia.tfmc_submissions:" + rootKey + "_leggings");
		config.set(root + ".boots", "ia.tfmc_submissions:" + rootKey + "_boots");
		config.set(root + ".item", null);
		config.save(file);
	}

	private static void upsertItem(
		File file,
		String slug,
		String display,
		String baseSet,
		String permission,
		List<String> colours,
		List<String> styles,
		boolean addName,
		String kind
	) throws IOException {
		FileConfiguration config = loadOrEmpty(file);
		String root = slug;
		config.set(root + ".name", display);
		writeColour(config, root, colours);
		writeStyles(config, root, styles);
		config.set(root + ".add-name", addName);
		config.set(root + ".set", baseSet);
		config.set(root + ".permission", permission);
		config.set(root + ".scroll", null);
		if ("gun".equals(kind)) {
			config.set(root + ".item", "gunskin(" + slug + ")");
		} else {
			config.set(root + ".item", "ia.tfmc_submissions:" + slug);
		}
		config.set(root + ".helmet", null);
		config.set(root + ".chestplate", null);
		config.set(root + ".leggings", null);
		config.set(root + ".boots", null);
		config.save(file);
	}

	private static void writeColour(FileConfiguration config, String root, List<String> colours) {
		if (colours == null || colours.isEmpty()) {
			config.set(root + ".colour", null);
			return;
		}
		if (colours.size() == 1) {
			config.set(root + ".colour", colours.get(0));
		} else {
			config.set(root + ".colour", new ArrayList<>(colours));
		}
	}

	private static void writeStyles(FileConfiguration config, String root, List<String> styles) {
		if (styles == null || styles.isEmpty()) {
			config.set(root + ".styles", null);
		} else {
			config.set(root + ".styles", new ArrayList<>(styles));
		}
	}

	private static FileConfiguration loadOrEmpty(File file) throws IOException {
		YamlConfiguration config = new YamlConfiguration();
		if (file.exists()) {
			try {
				config.load(file);
			} catch (InvalidConfigurationException e) {
				throw new IOException("invalid yaml: " + file.getAbsolutePath(), e);
			}
		}
		return config;
	}

	private static String require(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("missing " + field);
		}
		return value.trim();
	}
}
