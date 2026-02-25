package fr.aot.aotchatmod.listeners;

import fr.aot.aotchatmod.AOTChatMod;
import fr.aot.aotchatmod.managers.ConfigManager;
import fr.aot.aotchatmod.managers.SpamManager;
import fr.aot.aotchatmod.utils.ChatFilter;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final AOTChatMod plugin;
    private final ConfigManager cfg;
    private final SpamManager spamManager;
    private final ChatFilter filter;

    public ChatListener(AOTChatMod plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
        this.spamManager = plugin.getSpamManager();
        this.filter = new ChatFilter(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // Bypass total
        if (player.hasPermission("aotchatmod.bypass")) return;

        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());


        // ANTI-LIENS
        if (cfg.isAntiLinksEnabled() && !player.hasPermission("aotchatmod.links")) {
            if (filter.containsLink(rawMessage)) {
                event.setCancelled(true);
                player.sendMessage(cfg.getLinksMessage());
                plugin.logAction("[LIEN BLOQUÉ] " + player.getName() + ": " + rawMessage);
                return;
            }
        }

        // ANTI-SPAM
        if (cfg.isAntiSpamEnabled() && !player.hasPermission("aotchatmod.spam")) {
            if (spamManager.isSpam(player.getUniqueId(), rawMessage)) {
                event.setCancelled(true);
                player.sendMessage(cfg.getSpamMessage());
                plugin.logAction("[SPAM BLOQUÉ] " + player.getName() + ": " + rawMessage);
                return;
            }
        }

        // ANTI-MAJUSCULES
        if (cfg.isAntiCapsEnabled() && !player.hasPermission("aotchatmod.caps")) {
            if (filter.hasTooManyCaps(rawMessage)) {
                // Convertir
                event.message(Component.text(rawMessage.toLowerCase()));
                player.sendMessage(cfg.getCapsMessage());
            }
        }


        String currentMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        //  ANTI-COULEURS
        if (cfg.isAntiColorsEnabled() && !player.hasPermission("aotchatmod.color")) {
            if (filter.containsColorCodes(currentMessage)) {
                event.setCancelled(true);
                player.sendMessage(cfg.getColorsMessage());
                plugin.logAction("[COULEUR BLOQUÉE] " + player.getName() + ": " + currentMessage);
                return;
            }
        }

        // ANTI-INSULTES
        if (cfg.isAntiSwearEnabled()) {
            String msgToCheck = PlainTextComponentSerializer.plainText().serialize(event.message());
            String foundWord = filter.findForbiddenWord(msgToCheck);

            if (foundWord != null) {
                event.setCancelled(true);
                player.sendMessage(cfg.getSwearMessage());
                notifyStaff(player, msgToCheck);
                plugin.logAction("[INSULTE BLOQUÉE] " + player.getName() + ": " + msgToCheck);
                return;
            }
        }

        // Log message normal
        if (cfg.isLoggingEnabled()) {
            plugin.logAction("[CHAT] " + player.getName() + ": " +
                    PlainTextComponentSerializer.plainText().serialize(event.message()));
        }
    }

    //  Notif staff

    private void notifyStaff(Player player, String message) {
        if (!cfg.isSwearNotifyStaff()) return;
        String permission = cfg.getSwearStaffPermission();
        String notification = "§8[§bChatMod§8] §e" + player.getName() + " §7a tenté d'envoyer : §f" + message;
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission(permission) && !staff.equals(player)) {
                staff.sendMessage(notification);
            }
        }
    }
}
