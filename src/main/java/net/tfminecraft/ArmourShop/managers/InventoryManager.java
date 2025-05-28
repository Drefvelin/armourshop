package net.tfminecraft.ArmourShop.managers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.Plugins.TLibs.TLibs;
import me.Plugins.TLibs.Enums.APIType;
import me.Plugins.TLibs.Objects.API.ItemAPI;
import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;
import net.tfminecraft.ArmourShop.ArmourShop;
import net.tfminecraft.ArmourShop.Cache;
import net.tfminecraft.ArmourShop.enums.ArmorType;
import net.tfminecraft.ArmourShop.holder.ASInventoryHolder;
import net.tfminecraft.ArmourShop.loaders.CategoryLoader;
import net.tfminecraft.ArmourShop.objects.SkinCategory;
import net.tfminecraft.ArmourShop.objects.SkinSet;

public class InventoryManager {

	public void typeView(Player player) {
		Inventory i = ArmourShop.plugin.getServer().createInventory(new ASInventoryHolder(false), 9, "§7Armourshop Type");
		i.setItem(0, createArmourItem());
		i.setItem(1, createItemItem());
		player.openInventory(i);
	}
	public void categoryView(Player player, boolean item) {
		Inventory i = ArmourShop.plugin.getServer().createInventory(new ASInventoryHolder(item), 27, "§7Armourshop Categories");
		int c = 0;
		for(int y = 0; y<CategoryLoader.get().size();y++) {
			if(c > 26) break;
			SkinCategory cat = CategoryLoader.get().get(y);
			if(!(cat.isItem() == item)) continue;
			if(cat.hasPermission()) {
				while(cat.hasPermission() && !player.hasPermission(cat.getPermission())) {
					y++;
					if(y >= CategoryLoader.get().size()) break;
					SkinCategory next = CategoryLoader.get().get(y);
					if(!(next.isItem() == item)) continue;
					cat = next;
				}
			}
			if(!cat.hasPermission()) {
				i.setItem(c, createCategoryItem(cat));
			} else if(player.hasPermission(cat.getPermission())) {
				i.setItem(c, createCategoryItem(cat));
			}
			c++;
		}
		i.setItem(26, createBackButton());
		player.openInventory(i);
	}
	public void skinView(Player player, SkinCategory cat, int page, boolean item) {
		Inventory i = ArmourShop.plugin.getServer().createInventory(new ASInventoryHolder(item), 54, cat.getName());
		int slot = 0;
		while(slot < 9) {
			ItemStack fill = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
			ItemMeta fm = fill.getItemMeta();
			fm.setDisplayName("§8 ");
			fill.setItemMeta(fm);
			i.setItem(slot, fill);
			slot++;
		}
		int c = 0;
		List<Integer> points = new ArrayList<>();
		if(item) {
			points = Cache.itemPoints;
			c = page*Cache.itemPoints.size();
		} else {
			points = Cache.points;
			c = page*Cache.points.size();
		}
		
		for(Integer x : points) {
			if(c == cat.getSets().size()) break;
			SkinSet set = cat.getSets().get(c);
			if(set.hasItem()){
				i.setItem(x, createSkinItem(set, set.getItem(), ArmorType.ITEM));
				c++;
				continue;
			}
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
		if(cat.getSets().size()-(page*points.size()) > points.size()) {
			i.setItem(5, getPageItem("mcicons:icon_next_orange", page));
		}
		player.openInventory(i);
	}

	public ItemStack createArmourItem(){
		ItemStack i = new ItemStack(Material.IRON_CHESTPLATE, 1);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(StringFormatter.formatHex("#52de81§lArmour"));
		m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		List<String> lore = new ArrayList<>();
		lore.add("§7Armour skins");
		lore.add("");
		lore.add(StringFormatter.formatHex("#21de21Click to View"));
		m.setLore(lore);
		i.setItemMeta(m);
		return i;
	}

	public ItemStack createItemItem(){
		ItemStack i = new ItemStack(Material.IRON_SWORD, 1);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(StringFormatter.formatHex("#52de81§lItems"));
		m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		List<String> lore = new ArrayList<>();
		lore.add("§7Item skins");
		lore.add("");
		lore.add(StringFormatter.formatHex("#21de21Click to View"));
		m.setLore(lore);
		i.setItemMeta(m);
		return i;
	}
	
	public ItemStack createCategoryItem(SkinCategory c) {
		ItemAPI api = (ItemAPI) TLibs.getApiInstance(APIType.ITEM_API);
		ItemStack i = api.getCreator().getItemFromPath(c.getItem());
		if(i == null) i = new ItemStack(Material.DIRT, 1);
		ItemMeta meta = i.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.setDisplayName(c.getName());
		List<String> lore = new ArrayList<String>();
		if(c.isItem()) lore.add("§a"+c.getSets().size()+" §eItems");
		else lore.add("§a"+c.getSets().size()+" §eArmor Sets");
		meta.setLore(lore);
		i.setItemMeta(meta);
		return i;
	}
	
	public ItemStack createSkinItem(SkinSet set, String id, ArmorType type) {
		ItemStack i = null;
		ItemAPI api = (ItemAPI) TLibs.getApiInstance(APIType.ITEM_API);
		if(id.split("\\(")[0].equalsIgnoreCase("localmodel")){
			String info = id.split("\\(")[1].replace(")", "");
			try {
				i = new ItemStack(Material.valueOf(info.split("\\.")[0].toUpperCase()), 1);
				ItemMeta m = i.getItemMeta();
				m.setCustomModelData(Integer.parseInt(info.split("\\.")[1]));
				i.setItemMeta(m);
			} catch (Exception e) {
				e.printStackTrace();
				i = new ItemStack(Material.DIRT, 1);
			}
		} else{
			i = api.getCreator().getItemFromPath(id);
		}
		
		ItemMeta meta = i.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		if(type.equals(ArmorType.ITEM)) meta.setDisplayName(set.getName());
		else meta.setDisplayName(set.getName()+" "+ WordUtils.capitalize(type.toString().toLowerCase()));
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
