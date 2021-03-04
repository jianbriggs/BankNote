package com.ruinscraft.banknote;

import net.md_5.bungee.api.ChatColor;

/**
 * This class holds all "global" strings and other values used many times throughout
 * the plugin.
 * 
 * @author ian
 *
 */
public class DataSource {
	// Chat+GUI elements
	public static final String PLUGIN_BANNER = "" + ChatColor.GOLD + "--------------[ BankNote ]--------------";
	public static final String VALID_BANKNOTE = "" + ChatColor.GREEN + "This is a valid, authentic Bank Note";
	public static final String INVALID_BANKNOTE = "" + ChatColor.RED + "Sorry, but this Bank Note is a counterfeit!";
	public static final String NOT_A_BANKNOTE = "" + ChatColor.RED + "This item is not a Bank Note";
	public static final String CREATE_NO_ITEM = "" + ChatColor.RED + "You need an item in your hand to create a Bank Note";
	// BankNote book elements
	public static final String BANKNOTE_AUTHOR = "" + ChatColor.GOLD + "B" + ChatColor.YELLOW + "ank " + ChatColor.GOLD + "N" + ChatColor.YELLOW + "ote";
	public static final String BANKNOTE_LORE_COLOR = "" + ChatColor.LIGHT_PURPLE;
}
