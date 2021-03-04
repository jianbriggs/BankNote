package com.ruinscraft.banknote;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import net.md_5.bungee.api.ChatColor;

public class BankNote {
	private final static String MAGIC = "$29A$";
	private final static String TIME_VERIFY = "9a781a6610a73c2b691f5653a945d4bd";
	private String id;
	private UUID creator;
	private ItemStack item;
	private long timestamp;
	private int quantity;
	private boolean debug;
	
	public BankNote(Player player, ItemStack item, String secret) {
		this.creator = player.getUniqueId();
		this.item = item;
		this.timestamp = System.currentTimeMillis();
		this.quantity = item.getAmount();
		this.debug = false;
		this.id = generateHash(secret);
	}
	
	public BankNote(Player player, ItemStack item, String secret, boolean debug) {
		this.creator = player.getUniqueId();
		this.item = item;
		this.timestamp = System.currentTimeMillis();
		this.quantity = item.getAmount();
		this.debug = debug;
		this.id = generateHash(secret);
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
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void newId(String secret) {
		this.id = generateHash(secret);
	}

	private String generateHash(String secret) {
		return CryptoSecure.hash(this.creator.toString() + this.timestamp + secret + CryptoSecure.itemStackToBase64(this.item)); 
	}
	
	public static String generateHash(Player player, ItemStack item, String secret) {
		return CryptoSecure.hash(player.getUniqueId().toString() + System.currentTimeMillis() + secret + CryptoSecure.itemStackToBase64(item)); 
	}
	
	public boolean verifyHash(String secret) {
		String hash = generateHash(secret);
		return hash.equals(this.id);
	}
	
	public String timehash() {
		return CryptoSecure.hash("" + this.timestamp);
	}
	
	public boolean securityCheck() {
		return timehash().equalsIgnoreCase(TIME_VERIFY);
	}
	
	public void add(int amount) {
		this.quantity += amount;
	}
	
	public ItemStack exchangeItems(int amount) {
		if(quantity > 0 && quantity >= amount) {
			quantity -= amount;
			ItemStack items = this.item.clone();
			items.setAmount(amount);
			return items;
		}
		else return null;
	}
	
	public void setQuantity(int amount) {
		if(amount >= Integer.MAX_VALUE) {
			this.quantity = Integer.MAX_VALUE - 1;
		}
		else {
			this.quantity = amount;
		}
	}
	
	public int getQuantity() {
		return this.quantity;
	}
	
	public ItemStack getItem() {
		return this.item;
	}
	
	public void setItem(ItemStack item) {
		this.item = item;
	}
	
	/**
	 * Compares the Bank Note item to another item, ignoring amounts
	 * @param item
	 * @return
	 */
	public boolean itemEquals(ItemStack item) {
		return this.item.getItemMeta().equals(item.getItemMeta()) && this.item.getType().equals(item.getType());
	}
	
	public UUID getCreator() {
		return this.creator;
	}
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public String getFormattedDate() {
		Date date = new Date(this.timestamp);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
		return dateFormat.format(date);  
	}
	
	public boolean isEmpty() {
		return quantity == 0;
	}
	
	public static boolean isBankNote(BookMeta meta) {
		return meta.hasPages() && pageIsMeta(meta.getPage(1)) && meta.getPageCount() > 1;
	}
	
	public static boolean isSigned(BookMeta meta) {
		return meta.getGeneration().equals(Generation.TATTERED) && meta.getAuthor().equals(DataSource.BANKNOTE_AUTHOR) && meta.hasLore();
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
		
		String quantityLore = DataSource.BANKNOTE_LORE_COLOR + "QTY: ";
		
		if(this.quantity >= 1000000.0) {
			quantityLore += ChatColor.AQUA + String.format("%.1fm", (double)(this.quantity / 1000000.0));
		}
		else if(this.quantity >= 10000) {
			quantityLore += ChatColor.WHITE + String.format("%.1fk", (double)(this.quantity / 1000.0));
		}
		else {
			quantityLore += ChatColor.YELLOW + "" + this.quantity;
		}
		
		lore.add(quantityLore);
		
		bookMeta.setGeneration(Generation.TATTERED);
		bookMeta.setAuthor(DataSource.BANKNOTE_AUTHOR);
		bookMeta.setLore(lore);
		bookMeta.setTitle("" + ChatColor.YELLOW + this.item.getType());
		bookMeta.setPages(data);
		newBook.setItemMeta(bookMeta);
		
		return newBook;
	}
	
	public static boolean pageIsMeta(String s) {
		String[] tokens = s.split(";");
		if(tokens.length > 0) {
			return tokens.length == 7 && tokens[0].equals(MAGIC);
		}
		
		return false;
	}
	
	public String metaToString() {
		return MAGIC + ";" + this.id + ";" + this.creator.toString() + ";" + this.item.getType() + ";" + this.quantity + ";" + this.timestamp + ";" + this.debug;
	}
	
	private void setMeta(String s) {
		String[] tokens = s.split(";");
		if(tokens.length > 0) {
			this.id = tokens[1];
			this.creator = UUID.fromString(tokens[2]);
			this.quantity = Integer.parseInt(tokens[4]);
			this.timestamp = Long.parseLong(tokens[5]);
			this.debug = Boolean.parseBoolean(tokens[6]);
		}
	}
}
