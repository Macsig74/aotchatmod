package fr.aot.aotchatmod.managers;

import fr.aot.aotchatmod.AOTChatMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigManager {

    private final AOTChatMod plugin;


    private File wordsFile;
    private FileConfiguration wordsConfig;


    private final List<String> forbiddenWords = new ArrayList<>();
    private final List<Pattern> forbiddenPatterns = new ArrayList<>();
    private final List<String> whitelistWords = new ArrayList<>();
    private final List<String> bypassSeparators = new ArrayList<>();
    private final Map<Character, List<String>> leetspeakMap = new HashMap<>();

    public ConfigManager(AOTChatMod plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        // Reload main config
        plugin.reloadConfig();

        // Reload words.yml
        wordsFile = new File(plugin.getDataFolder(), "words.yml");
        if (!wordsFile.exists()) {
            plugin.saveResource("words.yml", false);
        }
        wordsConfig = YamlConfiguration.loadConfiguration(wordsFile);

        loadWords();
    }

    private void loadWords() {
        forbiddenWords.clear();
        forbiddenPatterns.clear();
        whitelistWords.clear();
        bypassSeparators.clear();
        leetspeakMap.clear();


        List<String> raw = wordsConfig.getStringList("forbidden-words");
        for (String word : raw) {
            if (word.startsWith("regex:")) {
                String regexStr = word.substring(6);
                try {
                    forbiddenPatterns.add(Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE));
                } catch (PatternSyntaxException e) {
                    plugin.getLogger().warning("Regex invalide dans words.yml : " + regexStr);
                }
            } else {
                forbiddenWords.add(word.toLowerCase());
            }
        }


        whitelistWords.addAll(wordsConfig.getStringList("whitelist-words")
                .stream().map(String::toLowerCase).toList());


        bypassSeparators.addAll(wordsConfig.getStringList("bypass-patterns.separators"));


        var leetspeakSection = wordsConfig.getConfigurationSection("bypass-patterns.leetspeak");
        if (leetspeakSection != null) {
            for (String key : leetspeakSection.getKeys(false)) {
                if (key.length() == 1) {
                    List<String> replacements = leetspeakSection.getStringList(key);
                    leetspeakMap.put(key.charAt(0), replacements);
                }
            }
        }
    }

    //Getters

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public String getPrefix() {
        return cfg().getString("prefix", "&8[&bAOTChatMod&8] &r");
    }

    public Component colorize(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public String getMessage(String key) {
        String raw = cfg().getString("messages." + key, "&cMessage introuvable : " + key);
        return raw.replace("&", "§");
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String msg = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return msg;
    }

    // Anti-links
    public boolean isAntiLinksEnabled() { return cfg().getBoolean("anti-links.enabled", true); }
    public boolean isLinksDetectObfuscated() { return cfg().getBoolean("anti-links.detect-obfuscated", true); }
    public List<String> getLinksWhitelist() { return cfg().getStringList("anti-links.whitelist"); }
    public String getLinksMessage() { return cfg().getString("anti-links.message", "&cLiens interdits !").replace("&", "§"); }

    // Anti-spam
    public boolean isAntiSpamEnabled() { return cfg().getBoolean("anti-spam.enabled", true); }
    public int getSpamMaxMessages() { return cfg().getInt("anti-spam.max-messages", 4); }
    public int getSpamTimeWindow() { return cfg().getInt("anti-spam.time-window", 5); }
    public int getSpamSimilarityThreshold() { return cfg().getInt("anti-spam.similarity-threshold", 85); }
    public long getSpamCooldown() { return cfg().getLong("anti-spam.cooldown", 800); }
    public String getSpamMessage() { return cfg().getString("anti-spam.message", "&cAnti-spam !").replace("&", "§"); }

    // Anti-caps
    public boolean isAntiCapsEnabled() { return cfg().getBoolean("anti-caps.enabled", true); }
    public int getCapsMaxPercentage() { return cfg().getInt("anti-caps.max-caps-percentage", 60); }
    public int getCapsMinLength() { return cfg().getInt("anti-caps.min-length", 8); }
    public String getCapsMessage() { return cfg().getString("anti-caps.message", "&cPas de majuscules !").replace("&", "§"); }

    // Anti-colors
    public boolean isAntiColorsEnabled() { return cfg().getBoolean("anti-colors.enabled", true); }
    public String getColorsMessage() { return cfg().getString("anti-colors.message", "&cCouleurs interdites !").replace("&", "§"); }

    // Anti-swear
    public boolean isAntiSwearEnabled() { return cfg().getBoolean("anti-swear.enabled", true); }
    public boolean isSwearDetectBypass() { return cfg().getBoolean("anti-swear.detect-bypass", true); }
    public String getSwearMessage() { return cfg().getString("anti-swear.message", "&cInsultes interdites !").replace("&", "§"); }
    public boolean isSwearNotifyStaff() { return cfg().getBoolean("anti-swear.notify-staff", true); }
    public String getSwearStaffPermission() { return cfg().getString("anti-swear.staff-notify-permission", "aotchatmod.admin"); }

    // Mute
    public boolean isMutePersist() { return cfg().getBoolean("mute.persist", true); }
    public String getMutedMessage() { return cfg().getString("mute.muted-message", "&cVous êtes réduit au silence.").replace("&", "§"); }
    public boolean isMuteBroadcast() { return cfg().getBoolean("mute.mute-broadcast", true); }
    public String getMuteBroadcastMessage() { return cfg().getString("mute.mute-broadcast-message", "").replace("&", "§"); }
    public String getUnmuteBroadcastMessage() { return cfg().getString("mute.unmute-broadcast-message", "").replace("&", "§"); }

    // Logging
    public boolean isLoggingEnabled() { return cfg().getBoolean("logging.enabled", true); }

    // Words

    public List<String> getForbiddenWords() { return forbiddenWords; }
    public List<Pattern> getForbiddenPatterns() { return forbiddenPatterns; }
    public List<String> getWhitelistWords() { return whitelistWords; }
    public List<String> getBypassSeparators() { return bypassSeparators; }
    public Map<Character, List<String>> getLeetspeakMap() { return leetspeakMap; }
}
