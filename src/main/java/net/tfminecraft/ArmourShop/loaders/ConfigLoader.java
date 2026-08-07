package net.tfminecraft.ArmourShop.loaders;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.Plugins.TLibs.Interface.LoaderInterface;
import net.tfminecraft.ArmourShop.Cache;

public class ConfigLoader implements LoaderInterface{
	public void load(File configFile) {
		FileConfiguration config = new YamlConfiguration();
        try {
        	config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        Cache.points = config.getIntegerList("start-points");
        Cache.itemPoints = config.getIntegerList("item-start-points");

        String base = config.getString("skins-api.base-url", "");
        if (base == null) {
            base = "";
        }
        base = base.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        Cache.skinsApiBaseUrl = base;

        String key = config.getString("skins-api.plugin-key", "");
        Cache.skinsPluginKey = key == null ? "" : key.trim();

        String iaPath = config.getString("pack-apply.ia-contents-path", "");
        Cache.iaContentsPath = iaPath == null ? "" : iaPath.trim();

        String catPath = config.getString("pack-apply.categories-path", "");
        Cache.categoriesPath = catPath == null ? "" : catPath.trim();
	}
}
