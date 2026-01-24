package tfmc.justin.managers;

import me.Plugins.TLibs.Enums.APIType;
import me.Plugins.TLibs.Objects.API.ItemAPI;
import me.Plugins.TLibs.TLibs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import tfmc.justin.config.GeigerConfiguration;
import tfmc.justin.handlers.ParticleRenderer;
import tfmc.justin.handlers.SourceHandler;
import tfmc.justin.validators.GeigerValidator;

// ====================================
// Main manager for the Geiger Counter system
// Coordinates between configuration, validation, particles, and source handling
// ====================================
public class GeigerManager {
    
    private static final long CHECK_INTERVAL_TICKS = 5L;
    
    private static GeigerManager instance;
    private final JavaPlugin plugin;
    
    // ===== COMPONENTS =====
    private GeigerConfiguration configuration;
    private GeigerValidator validator;
    private ParticleRenderer particleRenderer;
    private SourceHandler sourceHandler;
    private ItemAPI api;
    
    // ===== INITIALIZATION =====
    
    private GeigerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public static GeigerManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new GeigerManager(plugin);
        }
        return instance;
    }
    
    public static GeigerManager getInstance() {
        return instance;
    }
    
    // ====================================
    // Initialize the Geiger Counter system
    // ====================================
    public void initialize() {
        plugin.getLogger().info("[Init] Starting Geiger Counter initialization...");
        
        plugin.getLogger().info("[Init] Loading TLibs API...");
        api = (ItemAPI) TLibs.getApiInstance(APIType.ITEM_API);
        
        plugin.getLogger().info("[Init] Loading configuration...");
        configuration = new GeigerConfiguration(plugin);
        configuration.load();
        
        plugin.getLogger().info("[Init] Creating validator...");
        validator = new GeigerValidator(plugin, api);
        
        plugin.getLogger().info("[Init] Creating particle renderer...");
        particleRenderer = new ParticleRenderer(configuration);
        
        plugin.getLogger().info("[Init] Creating source handler...");
        sourceHandler = new SourceHandler(plugin, configuration, api);
        
        // Spawn initial source
        plugin.getLogger().info("[Init] Spawning initial radioactive source...");
        sourceHandler.moveSourceToRandomLocation();
        
        // Start player checking task
        plugin.getLogger().info("[Init] Starting player check task...");
        startPlayerCheckTask();
        
        plugin.getLogger().info("[Init] Geiger Counter initialization complete!");
    }
    
    private void startPlayerCheckTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkAllPlayers, 0L, CHECK_INTERVAL_TICKS);
    }
    
    // ===== PLAYER CHECKING =====
    
    // ====================================
    // Check all online players to see if they're holding a Geiger Counter
    // ====================================
    private void checkAllPlayers() {
        Location source = sourceHandler.getSourceLocation();
        if (source == null) {
            return;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            
            if (validator.isGeigerCounter(heldItem)) {
                handlePlayerWithGeiger(player, source);
            }
        }
    }
    
    private void handlePlayerWithGeiger(Player player, Location source) {
        double distance = calculateHorizontalDistance(player.getLocation(), source);
        particleRenderer.showParticleEffect(player, distance);
        sourceHandler.tryCollectSource(player, distance);
    }
    
    // ===== DISTANCE CALCULATION =====

    // Calculate horizontal distance between two locations (ignores Y axis)
    private double calculateHorizontalDistance(Location from, Location to) {
        double deltaX = from.getX() - to.getX();
        double deltaZ = from.getZ() - to.getZ();
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }
}
