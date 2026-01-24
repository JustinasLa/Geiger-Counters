package tfmc.justin;

import org.bukkit.plugin.java.JavaPlugin;
import tfmc.justin.managers.PluginManager;
import tfmc.justin.managers.GeigerManager;
import tfmc.justin.listeners.PlayerListener;

public class geiger_counter extends JavaPlugin {
    
    private static geiger_counter instance;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        GeigerManager.getInstance(this).initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        getLogger().info("geiger_counter has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("geiger_counter has been disabled!");
    }
    
    public static geiger_counter getInstance() {
        return instance;
    }
    
}