package tfmc.justin.config;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import tfmc.justin.models.ItemReward;
import tfmc.justin.models.TierReward;
import tfmc.justin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

// ====================================
// Handles loading and storing all Geiger Counter configuration
// ====================================
public class GeigerConfiguration {
    
    private final JavaPlugin plugin;
    
    // World settings
    private World world;
    private double minX, maxX, minZ, maxZ;
    
    // Detection settings
    private double collectionDistance;
    private double maxDetectionDistance;
    private double closeRangeThreshold;
    private double threeRingsDistance;
    private double twoRingsDistance;
    
    // Messages
    private String messageFoundSource;
    private String messageDeadGeiger;
    
    // Colors
    private ColorConfig closeRangeStartColor;
    private ColorConfig closeRangeEndColor;
    private ColorConfig farRangeStartColor;
    private ColorConfig farRangeEndColor;
    
    // Rewards
    private final List<TierReward> tierRewards = new ArrayList<>();
    
    public GeigerConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    // ====================================
    // Load all configuration from config.yml
    // ====================================
    public void load() {
        loadWorld();
        loadSearchArea();
        loadDetectionSettings();
        loadColorSettings();
        loadMessages();
        loadRewards();
    }
    
    private void loadWorld() {
        String worldName = plugin.getConfig().getString("source.world");
        world = Bukkit.getWorld(worldName);
    }
    
    private void loadSearchArea() {
        minX = plugin.getConfig().getDouble("source.top-left.x");
        maxX = plugin.getConfig().getDouble("source.bottom-right.x");
        minZ = plugin.getConfig().getDouble("source.top-left.z");
        maxZ = plugin.getConfig().getDouble("source.bottom-right.z");
        
        // Make sure that <min> is always less than <max>
        if (minX > maxX) {
            double temp = minX;
            minX = maxX;
            maxX = temp;
        }
        if (minZ > maxZ) {
            double temp = minZ;
            minZ = maxZ;
            maxZ = temp;
        }
    }
    
    private void loadDetectionSettings() {
        collectionDistance = plugin.getConfig().getDouble("detection.collection-distance", 20.0);
        maxDetectionDistance = plugin.getConfig().getDouble("detection.max-detection-distance", 2500.0);
        closeRangeThreshold = plugin.getConfig().getDouble("detection.close-range-threshold", 200.0);
        threeRingsDistance = plugin.getConfig().getDouble("detection.ring-thresholds.three-rings", 100.0);
        twoRingsDistance = plugin.getConfig().getDouble("detection.ring-thresholds.two-rings", 300.0);
    }
    
    private void loadMessages() {
        messageFoundSource = Utils.colorize(plugin.getConfig().getString("messages.found-source", 
            "&5You have found the source of Arcane Radiation! The source has moved."));
        messageDeadGeiger = Utils.colorize(plugin.getConfig().getString("messages.dead-geiger", 
            "&7Your Arcane Trace Detector has run out of fuel."));
    }
    
    // =========== Color Settings ======================
    private void loadColorSettings() {
        closeRangeStartColor = new ColorConfig(
            plugin.getConfig().getInt("colors.close-range.start.red", 255),
            plugin.getConfig().getInt("colors.close-range.start.green", 255),
            plugin.getConfig().getInt("colors.close-range.start.blue", 255)
        );
        closeRangeEndColor = new ColorConfig(
            plugin.getConfig().getInt("colors.close-range.end.red", 255),
            plugin.getConfig().getInt("colors.close-range.end.green", 0),
            plugin.getConfig().getInt("colors.close-range.end.blue", 255)
        );
        
        farRangeStartColor = new ColorConfig(
            plugin.getConfig().getInt("colors.far-range.start.red", 255),
            plugin.getConfig().getInt("colors.far-range.start.green", 0),
            plugin.getConfig().getInt("colors.far-range.start.blue", 255)
        );
        farRangeEndColor = new ColorConfig(
            plugin.getConfig().getInt("colors.far-range.end.red", 17),
            plugin.getConfig().getInt("colors.far-range.end.green", 0),
            plugin.getConfig().getInt("colors.far-range.end.blue", 17)
        );
    }
    
    // ====================================
    // Load tiered reward system from config
    // ====================================
    private void loadRewards() {
        tierRewards.clear();
        
        // Load tier weights
        ConfigurationSection weightsSection = plugin.getConfig().getConfigurationSection("drops.tier-weights");
        if (weightsSection == null) {
            plugin.getLogger().warning("No tier-weights section found in config!");
            return;
        }
        
        // Load tiers section
        ConfigurationSection tiersSection = plugin.getConfig().getConfigurationSection("drops.tiers");
        if (tiersSection == null) {
            plugin.getLogger().warning("No tiers section found in config!");
            return;
        }
        
        // Process each tier
        String[] tierNames = {"common", "uncommon", "rare", "epic", "legendary", "mythical"};
        for (String tierName : tierNames) {
            double weight = weightsSection.getDouble(tierName, 0.0);
            
            if (weight > 0) {
                TierReward tier = new TierReward(tierName, weight);
                
                // Load items for this tier
                List<String> tierItems = tiersSection.getStringList(tierName);
                for (String itemString : tierItems) {
                    ItemReward item = parseItemReward(itemString);
                    if (item != null) {
                        tier.addItem(item);
                    }
                }
                
                // Only add tier if it has items
                if (!tier.isEmpty()) {
                    tierRewards.add(tier);
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + tierRewards.size() + " reward tiers");
    }
    
    private ItemReward parseItemReward(String itemString) {
        String[] parts = itemString.split(":");
        if (parts.length >= 2) {
            String itemPath = parts[0] + ":" + parts[1];
            int amount = parseAmount(parts);
            return new ItemReward(itemPath, amount);
        }
        return null;
    }
    
    private int parseAmount(String[] parts) {
        if (parts.length >= 3) {
            try {
                return Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }
    
    // ===== GETTERS =====
    
    public World getWorld() { return world; }
    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinZ() { return minZ; }
    public double getMaxZ() { return maxZ; }
    public double getCollectionDistance() { return collectionDistance; }
    public double getMaxDetectionDistance() { return maxDetectionDistance; }
    public double getCloseRangeThreshold() { return closeRangeThreshold; }
    public double getThreeRingsDistance() { return threeRingsDistance; }
    public double getTwoRingsDistance() { return twoRingsDistance; }
    public String getMessageFoundSource() { return messageFoundSource; }
    public String getMessageDeadGeiger() { return messageDeadGeiger; }
    public ColorConfig getCloseRangeStartColor() { return closeRangeStartColor; }
    public ColorConfig getCloseRangeEndColor() { return closeRangeEndColor; }
    public ColorConfig getFarRangeStartColor() { return farRangeStartColor; }
    public ColorConfig getFarRangeEndColor() { return farRangeEndColor; }
    public List<TierReward> getTierRewards() { return tierRewards; }


    // =========== Store RGB color values ================
    public static class ColorConfig {
        private final int red;
        private final int green;
        private final int blue;
        
        public ColorConfig(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
        
        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
    }
}
