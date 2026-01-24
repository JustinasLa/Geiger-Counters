package tfmc.justin.validators;

import me.Plugins.TLibs.Objects.API.ItemAPI;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

// ====================================
// Validates if items are Geiger Counters
// ====================================
public class GeigerValidator {
    
    private static final String GEIGER_COUNTER_PATH = "m.TOOLS.GEIGER_COUNTER";
    
    private final JavaPlugin plugin;
    private final ItemAPI api;
    
    public GeigerValidator(JavaPlugin plugin, ItemAPI api) {
        this.plugin = plugin;
        this.api = api;
    }
    
    // ====================================
    // Check if an item is a valid Geiger Counter by matching against TLibs item path
    // ====================================
    public boolean isGeigerCounter(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        try {
            ItemStack geigerTemplate = api.getCreator().getItemFromPath(GEIGER_COUNTER_PATH).clone();
            return item.isSimilar(geigerTemplate);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to validate Geiger Counter: " + e.getMessage());
            return false;
        }
    }
}
