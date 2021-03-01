package com.ruinscraft.banknote;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BankNoteCommandExecutor implements CommandExecutor, TabCompleter{
	private final BankNote plugin;
	
	private final List<String> tabOptions;
	private final List<String> adminTabOptions;

	private final String PLUGIN_BANNER = "" + ChatColor.GOLD + "--------------[ BankNote ]--------------";
	
	public BankNoteCommandExecutor(BankNote plugin) {
		this.plugin = plugin;
		this.tabOptions = new ArrayList<String>();
		this.adminTabOptions = new ArrayList<String>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		
		if(args.length >= 1) {
			switch(args[0].toLowerCase()){
				case "create":
					convertItemToBankNote(player);
					break;
				case "exchange":
					// TODO: setup cmd handling for quantity
					int amount = 1;
					getItemFromBankNote(player, amount);
					break;
				default:
					showHelp(player);
					break;
			}
		}
		else {
			showHelp(player);
		}
		
		return true;
	}
	

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String option : tabOptions) {
                if (option.startsWith(args[0].toLowerCase())) {
                    completions.add(option);
                }
            }
        }
        return completions;
	}
	
	private void showHelp(Player player) {
		String[] commandHelpBase = {
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " withdraw ($)" + ChatColor.GRAY + ": Removes money from your ledger",
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " balance" + ChatColor.GRAY + ": Check your ledger balance",
			ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " top" + ChatColor.GRAY + ": View top 10 earners"
		};
		
		String[] commandHelpAdmin = {
		    ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " view recent" + ChatColor.GRAY + ": View ten most recent transactions for a shop",
		    ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " history (player)" + ChatColor.GRAY + ": View ten most recent transactions made by a player",
		    ChatColor.DARK_AQUA + "  /shop" + ChatColor.AQUA + " balance (player)" + ChatColor.GRAY + ": Check a player's ledger balance"    
		};
			
		if(player.isOnline()) {
			player.sendMessage(this.PLUGIN_BANNER);
			player.sendMessage(commandHelpBase);
			
			if(player.hasPermission("dukesmart.shop.admin")) {
				player.sendMessage(commandHelpAdmin);
			}
		}
	}
	
	/**
	 * Experimental - encodes/serializes an ItemStack in player's hand
	 * @param player
	 */
	private void convertItemToBankNote(Player player) {
		PlayerInventory inventory = player.getInventory();
		ItemStack heldItem = inventory.getItemInMainHand();
		List<String> data = CryptoSecure.encodeItemStack(heldItem);
		
		ItemStack holder = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) holder.getItemMeta();
		meta.setAuthor("WATCHBOX");
		meta.setTitle("(Placeholder)");
		meta.setPages(data);
		holder.setItemMeta(meta);
		
		inventory.addItem(holder);
	}
	
	/**
	 * Experimental - decodes/deserializes an ItemStack from Book string
	 * @param player
	 */
	private void getItemFromBankNote(Player player, int amount) {
		PlayerInventory inventory = player.getInventory();
		ItemStack heldItem = inventory.getItemInMainHand();

		if(heldItem.getItemMeta() instanceof BookMeta) {
			BookMeta meta = (BookMeta) heldItem.getItemMeta();
			if(meta.hasPages()) {
				List<String> data = meta.getPages();
				ItemStack decode = CryptoSecure.decodeItemStack(data);
				
				if(decode != null) {
					inventory.addItem(decode);
				}
			}
		}
		else {
			return;
		}
	}
	/**
	 * Checks if a given string is a number
	 * 
	 * @param str - String to check
	 * @return True if the string consists of only numbers, False otherwise
	 */
	private boolean stringIsNumeric(String str) {
		for(char c : str.toCharArray()) {
			if(!Character.isDigit(c)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Safely converts a string consisting of numeric values
	 * into an integer. If the value of the number is greater than
	 * an integer's max value, it will truncate the value to it.
	 * 
	 * @param str - String to check for numeric value
	 * @return int value of the string, or -1 on error
	 */
	private int safeStringToInt(String str) {
		if(stringIsNumeric(str)) {
			if(str.length() > 10) {
				str = str.substring(0, 10);
			}

			if(Double.parseDouble(str) > Integer.MAX_VALUE) {
				return Integer.MAX_VALUE - 1;
			}
			else {
				return Integer.parseInt(str);
			}
		}
		return -1;
	}
}