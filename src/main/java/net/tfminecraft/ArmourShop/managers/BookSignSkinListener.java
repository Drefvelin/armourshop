package net.tfminecraft.ArmourShop.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import dev.lone.itemsadder.api.CustomStack;
import net.tfminecraft.ArmourShop.ArmourShop;

/**
 * When a player signs an ItemsAdder book skin ({@code slug}), swap the stack to
 * {@code slug_signed} while keeping pages, title, author, display name, lore, and PDC.
 */
public final class BookSignSkinListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignBook(PlayerEditBookEvent event) {
		if (!event.isSigning()) {
			return;
		}

		Player player = event.getPlayer();
		int slot = event.getSlot();
		ItemStack current = player.getInventory().getItem(slot);
		if (current == null || current.getType().isAir()) {
			return;
		}

		CustomStack custom = CustomStack.byItemStack(current);
		if (custom == null) {
			return;
		}

		String namespace = custom.getNamespace();
		String id = custom.getId();
		if (namespace == null || namespace.isBlank() || id == null || id.isBlank()) {
			return;
		}
		if (id.endsWith("_signed")) {
			return;
		}

		String signedId = id + "_signed";
		CustomStack signed = CustomStack.getInstance(namespace + ":" + signedId);
		if (signed == null) {
			return;
		}

		ItemMeta prevMeta = current.getItemMeta();
		ItemMeta prevMetaClone = prevMeta != null ? prevMeta.clone() : null;
		String displayName = prevMeta != null && prevMeta.hasDisplayName()
			? prevMeta.getDisplayName()
			: null;
		List<String> lore = prevMeta != null && prevMeta.hasLore() && prevMeta.getLore() != null
			? new ArrayList<>(prevMeta.getLore())
			: null;
		BookMeta signedContent = event.getNewBookMeta();
		int amount = Math.max(1, current.getAmount());

		new BukkitRunnable() {
			@Override
			public void run() {
				if (!player.isOnline()) {
					return;
				}
				ItemStack signedStack = signed.getItemStack();
				if (signedStack == null || signedStack.getType().isAir()) {
					return;
				}
				signedStack = signedStack.clone();
				signedStack.setAmount(amount);

				ItemMeta meta = signedStack.getItemMeta();
				if (!(meta instanceof BookMeta bookMeta)) {
					player.getInventory().setItem(slot, signedStack);
					return;
				}

				if (signedContent != null) {
					bookMeta.setPages(signedContent.getPages());
					if (signedContent.hasTitle()) {
						bookMeta.setTitle(signedContent.getTitle());
					}
					if (signedContent.hasAuthor()) {
						bookMeta.setAuthor(signedContent.getAuthor());
					}
					if (signedContent.hasGeneration()) {
						bookMeta.setGeneration(signedContent.getGeneration());
					}
				}
				if (displayName != null) {
					bookMeta.setDisplayName(displayName);
				}
				if (lore != null) {
					bookMeta.setLore(lore);
				}
				if (prevMetaClone != null) {
					copyPdc(prevMetaClone, bookMeta);
				}

				signedStack.setItemMeta(bookMeta);
				player.getInventory().setItem(slot, signedStack);
				ArmourShop.plugin.getLogger().info(
					"[book-sign] " + player.getName() + " "
						+ namespace + ":" + id + " -> " + signedId
				);
			}
		}.runTaskLater(ArmourShop.plugin, 1L);
	}

	private static void copyPdc(ItemMeta from, ItemMeta to) {
		try {
			from.getPersistentDataContainer().copyTo(to.getPersistentDataContainer(), true);
		} catch (NoSuchMethodError | UnsupportedOperationException ignored) {
			// Older API without copyTo — display/lore already copied above.
		}
	}
}
