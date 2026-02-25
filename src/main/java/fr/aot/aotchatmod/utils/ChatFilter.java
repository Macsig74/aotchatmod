package fr.aot.aotchatmod.utils;

import fr.aot.aotchatmod.AOTChatMod;
import fr.aot.aotchatmod.managers.ConfigManager;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ChatFilter {

    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)(https?://|www\\.|ftp://)" +
                    "[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=]+" +
                    "|[a-zA-Z0-9\\-]+\\.(com|net|org|fr|io|gg|me|tv|co|info|biz|xyz|eu|dev|app)(/[^\\s]*)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OBFUSCATED_LINK_PATTERN = Pattern.compile(
            "(?i)[a-zA-Z0-9\\s\\-]+\\s*\\.\\s*(com|net|org|fr|io|gg|me|tv|co|info|biz|xyz|eu|dev|app)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&[0-9a-fA-FkKlLmMnNoOrR]");

    private final AOTChatMod plugin;
    private final ConfigManager cfg;

    public ChatFilter(AOTChatMod plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
    }




    public boolean containsLink(String message) {
        if (LINK_PATTERN.matcher(message).find()) {
            // Vérifier whitelist
            for (String allowed : cfg.getLinksWhitelist()) {
                if (message.toLowerCase().contains(allowed.toLowerCase())) {
                    // Si le lien trouvé est dans la whitelist, on ignore
                    return false;
                }
            }
            return true;
        }
        if (cfg.isLinksDetectObfuscated() && OBFUSCATED_LINK_PATTERN.matcher(message).find()) {
            return true;
        }
        return false;
    }


    public boolean containsColorCodes(String message) {
        return COLOR_CODE_PATTERN.matcher(message).find();
    }


    public String stripColorCodes(String message) {
        return COLOR_CODE_PATTERN.matcher(message).replaceAll("");
    }


    public boolean hasTooManyCaps(String message) {
        String letters = message.replaceAll("[^a-zA-Z]", "");
        if (letters.length() < cfg.getCapsMinLength()) return false;
        long caps = letters.chars().filter(Character::isUpperCase).count();
        int percentage = (int) ((caps * 100.0) / letters.length());
        return percentage > cfg.getCapsMaxPercentage();
    }




    public String findForbiddenWord(String message) {
        String normalized = normalizeMessage(message.toLowerCase());
        String[] words = normalized.split("\\s+");

        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9éàèùâêîôûäëïöüç]", "");
            if (cleanWord.isEmpty()) continue;


            if (cfg.getWhitelistWords().contains(cleanWord)) continue;


            for (String forbidden : cfg.getForbiddenWords()) {
                if (forbidden.contains("*")) {
                    // Wildcard
                    String regex = "(?i)" + Pattern.quote(forbidden).replace("\\*", ".*");
                    if (Pattern.matches(regex, cleanWord)) return forbidden;
                } else if (cleanWord.equals(forbidden) || normalized.contains(" " + forbidden + " ")
                        || normalized.startsWith(forbidden + " ") || normalized.endsWith(" " + forbidden)
                        || normalized.equals(forbidden)) {
                    return forbidden;
                }
            }
        }


        String normalizedFull = normalizeMessage(message.toLowerCase());
        for (String forbidden : cfg.getForbiddenWords()) {
            if (!forbidden.contains(" ")) continue; // déjà vérifié mot par mot
            String pattern = forbidden.contains("*")
                    ? "(?i)" + Pattern.quote(forbidden).replace("\\*", ".*")
                    : "(?i)\\b" + Pattern.quote(forbidden) + "\\b";
            if (Pattern.compile(pattern).matcher(normalizedFull).find()) {
                return forbidden;
            }
        }

        // Vérifier les regex
        for (Pattern p : cfg.getForbiddenPatterns()) {
            if (p.matcher(normalizedFull).find()) return p.pattern();
        }

        return null;
    }




    private String normalizeMessage(String message) {
        if (!cfg.isSwearDetectBypass()) return message;

        String result = message;

        // Retirer les séparateurs entre les lettres (m-e-r-d-e  merde)
        for (String sep : cfg.getBypassSeparators()) {
            result = result.replace(sep, "");
        }


        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            boolean replaced = false;
            for (Map.Entry<Character, List<String>> entry : cfg.getLeetspeakMap().entrySet()) {
                for (String leet : entry.getValue()) {
                    if (leet.length() == 1 && c == leet.charAt(0)) {
                        sb.append(entry.getKey());
                        replaced = true;
                        break;
                    }
                }
                if (replaced) break;
            }
            if (!replaced) sb.append(c);
        }

        return sb.toString();
    }
}