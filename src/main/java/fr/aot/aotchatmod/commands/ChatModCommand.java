package fr.aot.aotchatmod.commands;

import fr.aot.aotchatmod.AOTChatMod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ChatModCommand implements CommandExecutor, TabCompleter {

    private final AOTChatMod plugin;

    public ChatModCommand(AOTChatMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("aotchatmod.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
            }
            case "info" -> {
                sender.sendMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                sender.sendMessage("§b AOTChatMod §7v" + plugin.getDescription().getVersion());
                sender.sendMessage("§7Mode : §e" + (AOTChatMod.isFolia() ? "Folia" : "Paper"));
                sender.sendMessage("§7Anti-liens : §a" + (plugin.getConfigManager().isAntiLinksEnabled() ? "✔" : "✘"));
                sender.sendMessage("§7Anti-spam : §a" + (plugin.getConfigManager().isAntiSpamEnabled() ? "✔" : "✘"));
                sender.sendMessage("§7Anti-caps : §a" + (plugin.getConfigManager().isAntiCapsEnabled() ? "✔" : "✘"));
                sender.sendMessage("§7Anti-couleurs : §a" + (plugin.getConfigManager().isAntiColorsEnabled() ? "✔" : "✘"));
                sender.sendMessage("§7Anti-insultes : §a" + (plugin.getConfigManager().isAntiSwearEnabled() ? "✔" : "✘"));
                sender.sendMessage("§7Mots interdits : §e" + plugin.getConfigManager().getForbiddenWords().size());
                sender.sendMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§b AOTChatMod §7- Aide");
        sender.sendMessage("§e/chatmod reload §7- Recharger la configuration");
        sender.sendMessage("§e/chatmod info §7- Afficher les informations");
        sender.sendMessage("§e/mute <joueur> [durée] [raison] §7- Muter un joueur");
        sender.sendMessage("§e/unmute <joueur> §7- Démuter un joueur");
        sender.sendMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "info");
        }
        return List.of();
    }
}
