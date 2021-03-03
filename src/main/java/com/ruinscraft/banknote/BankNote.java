package com.ruinscraft.banknote;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import com.google.gson.Gson;

import net.md_5.bungee.api.ChatColor;

public class BankNote {
	private final static String MAGIC = "$19890504$";
	private String id;
	private UUID creator;
	private ItemStack item;
	private int quantity;
	private boolean debug;
	
	public BankNote(Player player, ItemStack item) {
		this.id = generateHash(player, item);
		this.creator = player.getUniqueId();
		this.item = item;
		this.quantity = item.getAmount();
		this.debug = false;
	}
	
	public BankNote(Player player, ItemStack item, boolean debug) {
		this.id = generateHash(player, item);
		this.creator = player.getUniqueId();
		this.item = item;
		this.quantity = item.getAmount();
		this.debug = debug;
	}
	
	public BankNote(BookMeta meta) {
		if(meta.hasPages() && meta.getPageCount() > 0) {
			List<String> pages = meta.getPages();
			String bankNoteMeta = pages.get(0);
			if(pageIsMeta(bankNoteMeta)) {
				setMeta(bankNoteMeta);
				this.item = CryptoSecure.decodeItemStack(pages.subList(1, pages.size()));
			}
		}
	}
	
	private String generateHash(Player player, ItemStack item) {
		return CryptoSecure.hash(CryptoSecure.hash(player.getUniqueId().toString()) + CryptoSecure.hash(CryptoSecure.itemStackToBase64(item))); 
	}
	
	public void add(int amount) {
		this.quantity += amount;
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
	
	public boolean isEmpty() {
		return quantity == 0;
	}
	
	public static boolean isBankNote(BookMeta meta) {
		// TODO: write algorithm to check more things besides pages
		return meta.hasPages()
				&& !meta.getGeneration().equals(Generation.COPY_OF_ORIGINAL)
				&& !meta.getGeneration().equals(Generation.COPY_OF_COPY)
				&& pageIsMeta(meta.getPage(1));
	}
	
	public static boolean isSigned(BookMeta meta) {
		return meta.getAuthor().equals(DataSource.BANKNOTE_AUTHOR) && meta.hasLore();
	}
	/**
	 * Generates a new Book item from the BankNote information
	 * @return
	 */
	public ItemStack newUpdatedBook() {
		ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) newBook.getItemMeta();
		
		List<String> data = new ArrayList<String>();
		data.add(metaToString());
		data.addAll(CryptoSecure.encodeItemStack(this.item));
		List<String> lore = new ArrayList<String>();
		lore.add(DataSource.BANKNOTE_LORE_COLOR + "QTY: " + ChatColor.WHITE + this.quantity);
		
		bookMeta.setGeneration(Generation.TATTERED);
		bookMeta.setAuthor(DataSource.BANKNOTE_AUTHOR);
		bookMeta.setLore(lore);
		bookMeta.setTitle("" + ChatColor.YELLOW + this.item.getType());
		bookMeta.setPages(data);
		newBook.setItemMeta(bookMeta);
		
		return newBook;
	}
	
	private static boolean pageIsMeta(String s) {
		String[] tokens = s.split(";");
		if(tokens.length > 0) {
			return tokens.length == 6 && tokens[0].equals(MAGIC);
		}
		
		return false;
	}
	
	private String metaToString() {
		return MAGIC + ";" + this.id + ";" + this.creator.toString() + ";" + this.item.getType() + ";" + this.quantity + ";" + this.debug;
	}
	
	private void setMeta(String s) {
		String[] tokens = s.split(";");
		if(tokens.length > 0) {
			this.id = tokens[1];
			this.creator = UUID.fromString(tokens[2]);
			this.quantity = Integer.parseInt(tokens[4]);
			this.debug = Boolean.parseBoolean(tokens[5]);
		}
	}
}
