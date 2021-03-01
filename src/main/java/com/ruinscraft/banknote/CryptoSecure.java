package com.ruinscraft.banknote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import net.md_5.bungee.api.ChatColor;

public class CryptoSecure {
	private static final int MAX_LENGTH = 240;
	private static final String ENCODE_DELIMITTER = "!";
	
	public static boolean isBankNote(BookMeta meta) {
		// TODO: write algorithm to check more things besides pages
		return meta.hasPages() && !meta.getGeneration().equals(Generation.COPY_OF_ORIGINAL) && !meta.getGeneration().equals(Generation.COPY_OF_COPY);
	}
	/**
	 * Computes a MD5 hash from a given String of data
	 * @param data - String data to compute
	 * @return String representing hash value, or null on failure
	 */
	public static String hash(String data) {
	    MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		    md.update(data.getBytes());
		    byte[] digest = md.digest();
		    char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	        char[] hexChars = new char[digest.length * 2];
	        for (int j = 0; j < digest.length; j++) {
	            int v = digest[j] & 0xFF;
	            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	        }
	        return new String(hexChars);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return null;
	}
	
	public static List<String> encodeItemStack(ItemStack item) {
		String base64 = itemStackToBase64(item);
		ArrayList<String> output = new ArrayList<String>();
		
		int i = 0;
		while(i < base64.length()) {
		    output.add(ENCODE_DELIMITTER + base64.substring(i, Math.min(i + MAX_LENGTH, base64.length())) + ENCODE_DELIMITTER);
		    i += MAX_LENGTH;
		}
		
		return output;
	}
	
	public static ItemStack decodeItemStack(List<String> data) {
		String buffer = "";
		for(String s: data) {
			if(s.contains(ENCODE_DELIMITTER)) {
				buffer += s.split(ENCODE_DELIMITTER)[1];
			}
		}
		
		try {
			return base64ToItemStack(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
    public static String itemStackToBase64(ItemStack stack) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(stack);

            // Serialize that array
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
        catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }
    
    public static ItemStack base64ToItemStack(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            try {
                return (ItemStack) dataInput.readObject();
            } finally {
                dataInput.close();
            }
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
    
    public static Object base64ToObject(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            ObjectInputStream dataInput = new ObjectInputStream(inputStream);
            try {
                return dataInput.readObject();
            } finally {
                dataInput.close();
            }
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
