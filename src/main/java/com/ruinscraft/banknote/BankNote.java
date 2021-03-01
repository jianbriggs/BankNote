package com.ruinscraft.banknote;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class BankNote extends JavaPlugin {
	private FileConfiguration config = getConfig();
	private BankNoteListener listener;

    @Override
    public void onEnable() {

    	this.listener = new BankNoteListener(this);
    	
    	this.getCommand("banknote").setExecutor(new BankNoteCommandExecutor(this));
    	this.getCommand("banknote").setTabCompleter(this);
    	
    	Bukkit.getPluginManager().registerEvents(this.listener, this);
    	
    	getLogger().info("BankNote has been enabled!");
    }

    @Override
    public void onDisable() {
    	getLogger().info("BankNote has been disabled!");
    }
}