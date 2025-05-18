package net.tfminecraft.ArmourShop.managers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.Plugins.TLibs.TLibs;
import me.Plugins.TLibs.Enums.APIType;
import me.Plugins.TLibs.Objects.API.ItemAPI;
import net.tfminecraft.ArmourShop.ArmourShop;
import net.tfminecraft.ArmourShop.Cache;
import net.tfminecraft.ArmourShop.enums.ArmorType;
import net.tfminecraft.ArmourShop.loaders.CategoryLoader;
import net.tfminecraft.ArmourShop.objects.SkinCategory;
import net.tfminecraft.ArmourShop.objects.SkinSet;

public class InventoryManager {
	public void categoryView(Player player) {
		Inventory i = ArmourShop.plugin.getServer().createInventory(null, 27, "§7Armourshop Categories");
		int c = 0;
		for(int y = 0; y<CategoryLoader.get().size();y++) {
			SkinCategory cat = CategoryLoader.get().get(y);
			if(cat.hasPermission()) {
				while(cat.hasPermission() && !player.hasPermission(cat.getPermission())) {
					y++;
					if(y >= CategoryLoader.get().size()) break;
					cat = CategoryLoader.get().get(y);
				}
			}
			if(!cat.hasPermission()) {
				i.setItem(c, createCategoryItem(cat));
			} else if(player.hasPermission(cat.getPermission())) {
				i.setItem(c, createCategoryItem(cat));
			}
			c++;
		}
		player.openInventory(i);
	}
	public void skinView(Player player, SkinCategory cat, int page) {
		Inventory i = ArmourShop.plugin.getServer().createInventory(null, 54, cat.getName());
		int slot = 0;
		while(slot < 9) {
			ItemStack fill = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
			ItemMeta fm = fill.getItemMeta();
			fm.setDisplayName("§8 ");
			fill.setItemMeta(fm);
			i.setItem(slot, fill);
			slot++;
		}
		int c = page*Cache.points.size();
		for(Integer x : Cache.points) {
			if(c == cat.getSets().size()) break;
			SkinSet set = cat.getSets().get(c);
			if(set.hasHelmet()) {
				i.setItem(x, createSkinItem(set, set.getHelmet(), ArmorType.HELMET));
			}
			x++;
			if(set.hasChestplate()) {
				i.setItem(x, createSkinItem(set, set.getChestplate(), ArmorType.CHESTPLATE));
			}
			x++;
			if(set.hasLeggings()) {
				i.setItem(x, createSkinItem(set, set.getLeggings(), ArmorType.LEGGINGS));
			}
			x++;
			if(set.hasBoots()) {
				i.setItem(x, createSkinItem(set, set.getBoots(), ArmorType.BOOTS));
			}
			c++;
		}
		i.setItem(4, createBackButton());
		if(page > 0) {
			i.setItem(3, getPageItem("mcicons:icon_back_orange", page));
		}
		if(cat.getSets().size()-(page*Cache.points.size()) > Cache.points.size()) {
			i.setItem(5, getPageItem("mcicons:icon_next_orange", page));
		}
		player.openInventory(i);
	}
	
	public ItemStack createCategoryItem(SkinCategory c) {
		ItemAPI api = (ItemAPI) TLibs.getApiInstance(APIType.ITEM_API);
		ItemStack i = api.getCreator().getItemFromPath(c.getItem());
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(c.getName());
		List<String> lore = new ArrayList<String>();
		lore.add("§a"+c.getSets().size()+" §eArmor Sets");
		meta.setLore(lore);
		i.setItemMeta(meta);
		return i;
	}
	
	public ItemStack createSkinItem(SkinSet set, String id, ArmorType type) {
		ItemAPI api = (ItemAPI) TLibs.getApiInstance(APIType.ITEM_API);
		ItemStack i = api.getCreator().getItemFromPath(id);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(set.getName()+" "+ WordUtils.capitalize(type.toString().toLowerCase()));
		List<String> lore = new ArrayList<String>();
		lore.add("§eTier: §f"+WordUtils.capitalize(set.getSet().getId()));
		if(set.hasScroll()) {
			lore.add(" ");
			lore.add("§7Scroll: "+ api.getCreator().getItemFromPath(set.getScroll()).getItemMeta().getDisplayName());
		}
		NamespacedKey key = new NamespacedKey(ArmourShop.plugin, "set");
		meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, set.getId()+"."+type.toString().toLowerCase());
		meta.setLore(lore);
		i.setItemMeta(meta);
		return i;
	}
	
	public ItemStack getPageItem(String s, int page) {
		ItemAPI api = (ItemAPI) TLibs.getApiInstance(APIType.ITEM_API);
		ItemStack i = api.getCreator().getItemsAdderItem(s);
		ItemMeta m = i.getItemMeta();
		NamespacedKey key = new NamespacedKey(ArmourShop.plugin, "page");
		m.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, page);
		i.setItemMeta(m);
		return i;
	}
	
	public ItemStack createBackButton() {
		ItemStack i = new ItemStack(Material.BARRIER, 1);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName("§cBACK");
		i.setItemMeta(m);
		return i;
	}
}
