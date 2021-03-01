package com.ruinscraft.banknote;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import net.md_5.bungee.api.ChatColor;

public class BankNote {
	private BookMeta meta;
	private ItemStack item;
	private int quantity;
	
	public BankNote(BookMeta meta) {
		this.meta = meta;
		this.item = CryptoSecure.decodeItemStack(meta.getPages());
		this.quantity = getQuantityFromMeta();
	}
	
	public ItemStack exchangeItems(int amount) {
		if(quantity >= amount) {
			quantity -= amount;
			ItemStack items = this.item.clone();
			items.setAmount(amount);
			return items;
		}
		else return null;
	}
	
	public int getQuantity() {
		return this.quantity;
	}
	
	private int getQuantityFromMeta() {
		List<String> lore = this.meta.getLore();
		String qtyLine = ChatColor.stripColor(lore.get(0));
		String[] tokens = qtyLine.split(":");
		return Integer.parseInt(tokens[1].strip());
	}
	
	public boolean isEmpty() {
		return quantity == 0;
	}
	
	public static boolean isBankNote(BookMeta meta) {
		// TODO: write algorithm to check more things besides pages
		return meta.hasPages() && !meta.getGeneration().equals(Generation.COPY_OF_ORIGINAL) && !meta.getGeneration().equals(Generation.COPY_OF_COPY);
	}
	
	/**
	 * Generates a new Book item from the BankNote information
	 * @return
	 */
	public ItemStack newUpdatedBook() {
		ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);

		List<String> data = CryptoSecure.encodeItemStack(this.item);
		
		List<String> lore = new ArrayList<String>();
		lore.add(DataSource.BANKNOTE_LORE_COLOR + "QTY: " + ChatColor.WHITE + this.quantity);
		
		meta.setGeneration(Generation.TATTERED);
		meta.setAuthor(DataSource.BANKNOTE_AUTHOR);
		meta.setLore(lore);
		meta.setTitle("" + ChatColor.YELLOW + this.item.getType());
		meta.setPages(data);
		newBook.setItemMeta(meta);
		
		return newBook;
	}
}
