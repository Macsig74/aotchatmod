package fr.aot.aotchatmod.managers;

import fr.aot.aotchatmod.AOTChatMod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpamManager {

    private final AOTChatMod plugin;

    // Historique des mess
    private final Map<UUID, List<Long>> messageTimestamps = new ConcurrentHashMap<>();

    private final Map<UUID, String> lastMessages = new ConcurrentHashMap<>();

    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

    public SpamManager(AOTChatMod plugin) {
        this.plugin = plugin;
    }

    /**
     * la ca vérifie si le message est considéré comme du spam.
     * @return true si spam
     */
    public boolean isSpam(UUID uuid, String message) {
        long now = System.currentTimeMillis();
        int timeWindowMs = plugin.getConfigManager().getSpamTimeWindow() * 1000;
        int maxMessages = plugin.getConfigManager().getSpamMaxMessages();
        long cooldown = plugin.getConfigManager().getSpamCooldown();


        Long last = lastMessageTime.get(uuid);
        if (last != null && (now - last) < cooldown) {
            return true;
        }


        List<Long> timestamps = messageTimestamps.computeIfAbsent(uuid, k -> new ArrayList<>());

        timestamps.removeIf(t -> (now - t) > timeWindowMs);
        timestamps.add(now);

        if (timestamps.size() > maxMessages) {
            return true;
        }


        String lastMsg = lastMessages.get(uuid);
        if (lastMsg != null) {
            int similarity = getSimilarity(lastMsg, message);
            if (similarity >= plugin.getConfigManager().getSpamSimilarityThreshold()) {
                return true;
            }
        }

        lastMessages.put(uuid, message);
        lastMessageTime.put(uuid, now);
        return false;
    }


     // Levenshtein simplifié .

    public int getSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 100;
        if (s1.isEmpty() || s2.isEmpty()) return 0;

        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int maxLen = Math.max(s1.length(), s2.length());
        int distance = levenshteinDistance(s1, s2);
        return (int) ((1.0 - (double) distance / maxLen) * 100);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[] dp = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) dp[j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int temp = dp[j];
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[j] = prev;
                } else {
                    dp[j] = 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                }
                prev = temp;
            }
        }
        return dp[s2.length()];
    }

    public void clear() {
        messageTimestamps.clear();
        lastMessages.clear();
        lastMessageTime.clear();
    }

    public void clearPlayer(UUID uuid) {
        messageTimestamps.remove(uuid);
        lastMessages.remove(uuid);
        lastMessageTime.remove(uuid);
    }
}
