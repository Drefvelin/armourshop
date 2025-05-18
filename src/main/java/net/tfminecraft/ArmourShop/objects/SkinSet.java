package net.tfminecraft.ArmourShop.objects;

import java.util.Optional;

import org.bukkit.configuration.ConfigurationSection;

import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;
import net.tfminecraft.ArmourShop.loaders.BaseSetLoader;

public class SkinSet {
	private String id;
	private String name;
	private BaseSet set;
	private Optional<String> scroll = Optional.empty();
	private Optional<String> helmet = Optional.empty();
	private Optional<String> chestplate = Optional.empty();
	private Optional<String> leggings = Optional.empty();
	private Optional<String> boots = Optional.empty();
	private Optional<String> item = Optional.empty();
	private boolean addName;
	
	public SkinSet(String key, ConfigurationSection config) {
		this.id = key;
		this.name = StringFormatter.formatHex(config.getString("name"));
		this.set = BaseSetLoader.getByString(config.getString("set"));
		if(config.contains("scroll")) {
			scroll = Optional.of(config.getString("scroll"));
		}
		if(config.contains("helmet")) {
			helmet = Optional.of(config.getString("helmet"));
		}
		if(config.contains("chestplate")) {
			chestplate = Optional.of(config.getString("chestplate"));
		}
		if(config.contains("leggings")) {
			leggings = Optional.of(config.getString("leggings"));
		}
		if(config.contains("boots")) {
			boots = Optional.of(config.getString("boots"));
		}
		if(config.contains("item")){
			item = Optional.of(config.getString("item"));
		}
		if(config.contains("add-name")) {
			addName = config.getBoolean("add-name");
		} else {
			addName = false;
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public BaseSet getSet() {
		return set;
	}

	public String getScroll() {
		return scroll.get();
	}

	public String getHelmet() {
		return helmet.get();
	}

	public String getChestplate() {
		return chestplate.get();
	}

	public String getLeggings() {
		return leggings.get();
	}

	public String getBoots() {
		return boots.get();
	}

	public String getItem() {
		return item.get();
	}
	
	public boolean hasScroll() {
		return scroll.isPresent();
	}
	public boolean hasHelmet() {
		return helmet.isPresent();
	}
	public boolean hasChestplate() {
		return chestplate.isPresent();
	}
	public boolean hasLeggings() {
		return leggings.isPresent();
	}
	public boolean hasBoots() {
		return boots.isPresent();
	}
	public boolean hasItem() {
		return item.isPresent();
	}

	public boolean addName() {
		return addName;
	}
}
