package net.tfminecraft.ArmourShop;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.tfminecraft.ArmourShop.loaders.BaseSetLoader;
import net.tfminecraft.ArmourShop.loaders.CategoryLoader;
import net.tfminecraft.ArmourShop.loaders.ConfigLoader;
import net.tfminecraft.ArmourShop.loaders.SkinSetLoader;
import net.tfminecraft.ArmourShop.managers.CommandManager;
import net.tfminecraft.ArmourShop.managers.SkinManager;

public class ArmourShop extends JavaPlugin{
	public static ArmourShop plugin;
	
	private final SkinSetLoader skinSetLoader = new SkinSetLoader();
	private final CategoryLoader categoryLoader = new CategoryLoader();
	private final BaseSetLoader baseSetLoader = new BaseSetLoader();
	private final ConfigLoader configLoader = new ConfigLoader();
	
	private final CommandManager commandManager = new CommandManager();
	private final SkinManager skinManager = new SkinManager();
	
	@Override
	public void onEnable() {
		plugin = this;
		createFolders();
		createConfigs();
		loadConfigs();
		registerListeners();
		getCommand(commandManager.cmd1).setExecutor(commandManager);
	}
	
	public void registerListeners() {
		getServer().getPluginManager().registerEvents(commandManager, this);
		getServer().getPluginManager().registerEvents(skinManager, this);
	}
	public void loadConfigs() {
		configLoader.load(new File(getDataFolder(), "config.yml"));
		categoryLoader.load(new File(getDataFolder(), "categories.yml"));
		baseSetLoader.load(new File(getDataFolder(), "base-sets.yml"));
		File folder = new File(getDataFolder(), "Categories");
    	for (final File file : folder.listFiles()) {
    		if(!file.isDirectory()) {
    			skinSetLoader.load(file);
    		}
    	}
	}
	public void createFolders() {
		if (!getDataFolder().exists()) getDataFolder().mkdir();
		File subFolder = new File(getDataFolder(), "Categories");
		if(!subFolder.exists()) subFolder.mkdir();
	}
	
	public void createConfigs() {
		String[] files = {
				"categories.yml",
				"config.yml",
				"base-sets.yml",
				};
		for(String s : files) {
			File newConfigFile = new File(getDataFolder(), s);
	        if (!newConfigFile.exists()) {
	        	newConfigFile.getParentFile().mkdirs();
	            saveResource(s, false);
	        }
		}
	}
	
	public void reload() {
		loadConfigs();
	}
	public void reloadMessage(Player p) {
		p.sendMessage(ChatColor.GREEN + "[ArmourShop]" + ChatColor.YELLOW + " Reloading plugin...");
		reload();
		p.sendMessage(ChatColor.GREEN + "[ArmourShop]" + ChatColor.YELLOW + " Reloading complete!");
	}
}
