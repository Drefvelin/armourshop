package net.tfminecraft.ArmourShop;

import java.util.ArrayList;
import java.util.List;

public class Cache {
	public static List<Integer> points = new ArrayList<>();
	public static List<Integer> itemPoints = new ArrayList<>();

	/** ProvinceSystem base URL without trailing slash. */
	public static String skinsApiBaseUrl = "";
	/** X-Plugin-Key for ProvinceSystem skins routes. */
	public static String skinsPluginKey = "";

	/** Absolute path to ItemsAdder contents/ (parent of namespace folders). */
	public static String iaContentsPath = "";
	/** Absolute path to ArmourShop Categories/ (Step 8; may be empty). */
	public static String categoriesPath = "";
}
