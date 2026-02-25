package fr.aot.aotchatmod;

import fr.aot.aotchatmod.commands.ChatModCommand;
import fr.aot.aotchatmod.listeners.ChatListener;
import fr.aot.aotchatmod.managers.ConfigManager;
import fr.aot.aotchatmod.managers.SpamManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AOTChatMod extends JavaPlugin {

    private static AOTChatMod instance;

    private ConfigManager configManager;

    private SpamManager spamManager;

    @Override
    public void onEnable() {
        instance = this;

        //configurations par défaut
        saveDefaultConfig();
        saveResource("words.yml", false);

        // managers
        configManager = new ConfigManager(this);

        spamManager = new SpamManager(this);

        // listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // commandes
        getCommand("chatmod").setExecutor(new ChatModCommand(this));



        getLogger().info("AOTChatMod v" + getDescription().getVersion() + " activé avec succès !");
        getLogger().info("Compatible Paper 1.21.x & Folia");
    }

    @Override
    public void onDisable() {

        getLogger().info("AOTChatMod désactivé.");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        spamManager.clear();
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public static AOTChatMod getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SpamManager getSpamManager() {
        return spamManager;
    }


    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    //logs
    public void logAction(String message) {
        if (configManager.isLoggingEnabled()) {
            getLogger().info("[LOG] " + message);
        }
    }
}
