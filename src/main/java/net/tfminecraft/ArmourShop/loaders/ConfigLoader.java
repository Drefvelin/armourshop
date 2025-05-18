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
	}
}
