package com.ruinscraft.banknote;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class BankNotePlugin extends JavaPlugin {
	private FileConfiguration config = getConfig();
	private BankNoteListener listener;

    @Override
    public void onEnable() {
    	if(!config.contains("values.identifier") && !config.contains("values.secret")) {
    		config.options().copyDefaults(true);
            saveConfig();
    	}
    	
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
    
    public String getSecret() {
    	if(config.contains("values.secret")) {
    		return config.getString("values.secret");
    	}
    	else return null;
    }
    
    public String getIdentifier() {
    	if(config.contains("values.identifier")) {
    		return config.getString("values.identifier");
    	}
    	else return null;
    }
}