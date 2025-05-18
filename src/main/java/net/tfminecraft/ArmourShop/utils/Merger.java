package net.tfminecraft.ArmourShop.utils;

import java.util.Optional;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import dev.lone.LoneLibs.nbt.nbtapi.NBT;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import me.Plugins.TLibs.TLibs;
import me.Plugins.TLibs.Enums.APIType;
import me.Plugins.TLibs.Objects.API.ItemAPI;

public class Merger {

	public ItemStack merge(ItemStack item, Optional<String> name, String s) {
		ItemAPI api = (ItemAPI) TLibs.getApiInstance(APIType.ITEM_API);
		ItemStack skin = api.getCreator().getItemFromPath(s);
		if(s.split("\\.")[0].equalsIgnoreCase("ia")) {
			String namespace = s.split("\\.")[1].split("\\:")[0];
			String id = s.split("\\.")[1].split("\\:")[1];
			NBTItem mnbt = NBTItem.get(item);
			mnbt.addTag(new ItemTag("ia", namespace+"."+id));
			item = mnbt.toItem();
		}
		item.setType(skin.getType());
		ItemMeta skinMeta = skin.getItemMeta();
		ItemMeta m = item.getItemMeta();
		if(name.isPresent()) {
			m.setDisplayName(name.get());
		}
		m.setCustomModelData(skinMeta.getCustomModelData());
		if(skin.getType().toString().toLowerCase().contains("leather")) {
			LeatherArmorMeta ls = (LeatherArmorMeta) skinMeta;
			LeatherArmorMeta lm = (LeatherArmorMeta) m;
			lm.setColor(ls.getColor());
			item.setItemMeta(lm);
			if(s.split("\\.")[0].equalsIgnoreCase("ia")) {
				String namespace = s.split("\\.")[1].split("\\:")[0];
				String id = s.split("\\.")[1].split("\\:")[1];
				NBT.modify(item, nbt ->{
					nbt.getOrCreateCompound("itemsadder");
					nbt.getCompound("itemsadder").setString("namespace", namespace);
					nbt.getCompound("itemsadder").setString("id", id);
				});
			}
		} else {
			item.setItemMeta(m);
		}
		return item;
	}

}
