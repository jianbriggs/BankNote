package com.ruinscraft.banknote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.md_5.bungee.api.ChatColor;

public class BankNoteListener implements Listener{
	private BankNote plugin;
	
    public BankNoteListener(BankNote plugin) {
    	this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer(); // The player who joined
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
    	Player player = evt.getPlayer();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        Player player = evt.getPlayer();

        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
        }
    }
    
    private String getItemDisplayName(ItemStack item) {
    	ItemMeta meta = item.getItemMeta();
    	Material mat = item.getType();
    	
    	// Custom/display names
		if(meta.hasDisplayName()) {
			return "" + ChatColor.ITALIC + meta.getDisplayName();
		}
		// Written book names
		else if(itemIsFinishedBook(item)) {
			BookMeta bookMeta = (BookMeta) meta;
			if(bookMeta.hasTitle()) {
				return "" + ChatColor.ITALIC + bookMeta.getTitle();
			}
		}
		// Potion names
		else if(itemIsPotion(item)) {
			return "" + ChatColor.DARK_RED + getPotionName(item);
		}
		// fix for raw meats
		else if((mat.name().contains("PORK") || mat.name().contains("CHICKEN") || mat.name().contains("MUTTON") || mat.name().contains("BEEF") || mat.name().equals("RABBIT") || mat.name().contains("SALMON") || mat.name().contains("COD")) && !mat.name().contains("COOKED")) {
			XMaterial itemMaterial = XMaterial.matchXMaterial(item);
			return "Raw " + materialPrettyPrint(itemMaterial.parseMaterial());
		}
		// fix for chestplates
		else if(mat.name().contains("CHESTPLATE")) {
			XMaterial itemMaterial = XMaterial.matchXMaterial(item);
			String[] temp = materialPrettyPrint(itemMaterial.parseMaterial()).split(" ");
			return temp[0] + " Chest.";
		}
		// fix for green, red, and yellow dyes
		else if(mat.name().equals("CACTUS_GREEN")) {
			return "Green Dye";
		}
		else if(mat.name().equals("DANDELION_YELLOW")) {
			return "Yellow Dye";
		}
		else if(mat.name().equals("ROSE_RED")) {
			return "Red Dye";
		}
		
		XMaterial itemMaterial = XMaterial.matchXMaterial(item);
		return materialPrettyPrint(itemMaterial.parseMaterial());
	}

    /**
     * Checks whether a block is a chest or double chest
     * (note that Enderchests are not checked)
     * @param block - Block to check
     * @return True if block is chest, False otherwise
     */
    private boolean blockIsChest(Block block) {
    	return block != null && (block.getState() instanceof Chest || block.getState() instanceof DoubleChest);
    }
    
    private String materialPrettyPrint(Material material) {
    	String[] words = material.toString().split("_");
    	String output = "";
    	
    	for( String word : words) {
    		output += word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase() + " ";
    	}
    	output = output.trim();
    	return output;
    }
    
	private boolean playerCanStoreItem(Player player, ItemStack itemToBuy, short quantity) {
		PlayerInventory inv = player.getInventory();
		int maxStackSize = itemToBuy.getMaxStackSize();

		for(ItemStack item : inv.getContents()) {
			if(item == null || itemIsAir(item)) {
				continue;
			}
			// check if the slot's amount + quantity is
			// less than or equal to 64 (full stack)
			else if(item.isSimilar(itemToBuy) && item.getAmount() + quantity <= maxStackSize) {
				return true;
			}
		}
		
		// otherwise, return if there's a free, empty slot
		return inv.firstEmpty() >= 0;
	}
 
	private String truncateText(String message) {
    	if(message.length() >= 38) {
    		return message.substring(0, 34) + "...";
    	}
    	else {
    		return message;
    	}
    }
    
    private String prettyPrint(String message) {
    	String[] words = message.split("_");
    	String  output = "";
    	
    	for( String word : words) {
    		output += word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase() + " ";
    	}
    	output = output.trim();
    	return output;
    }

    private boolean itemIsFinishedBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.WRITTEN_BOOK.parseMaterial());
    }
    
    private boolean itemIsWritableBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.WRITABLE_BOOK.parseMaterial());
    }
    
    private boolean itemIsAir(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.AIR.parseMaterial());
    }
    
    private boolean itemIsBanner(ItemStack item) {
    	return item != null && item.getType().name().contains("BANNER");
    }
    
    private boolean itemIsShield(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.SHIELD.parseMaterial());
    }
    
    private boolean itemIsPotion(ItemStack item) {
		return item != null && item.getType().name().contains("POTION");
	}
    
    private boolean itemIsFilledMap(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.FILLED_MAP.parseMaterial());
    }
    
    private boolean itemIsShulkerBox(ItemStack item) {
    	return item != null && item.getType().name().contains("SHULKER_BOX");
    }
    
    private boolean itemIsEnchantedBook(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.ENCHANTED_BOOK.parseMaterial());
    }
    
    private boolean itemIsTippedArrow(ItemStack item) {
    	return item != null && item.getType().equals(XMaterial.TIPPED_ARROW.parseMaterial());
    }
    
    private String getPotionName(ItemStack potion) {
    	PotionMeta meta = (PotionMeta) potion.getItemMeta();
    	String name = prettyPrint(meta.getBasePotionData().getType().name());
    	
    	if(meta.getBasePotionData().isUpgraded()) {
    		name += " II";
    	}
    	
    	if(meta.getBasePotionData().isExtended()) {
    		name += " (Extended)";
    	}
    	
    	return name;
    }
}