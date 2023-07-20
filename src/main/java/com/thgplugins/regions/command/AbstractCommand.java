package com.thgplugins.regions.command;

import com.thgplugins.regions.RegionsPlugin;
import io.papermc.paper.text.PaperComponents;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractCommand extends Command implements PluginIdentifiableCommand {

    private final Plugin plugin;
    private boolean sendUsageMessage;

    public AbstractCommand(@NotNull Plugin plugin, @NotNull String name, @Nullable String description, @Nullable String usageMessage, @NotNull List<String> aliases) {
        super(name, (description == null) ? "" : description, (usageMessage == null) ? "/" + name : usageMessage, aliases);
        this.sendUsageMessage = usageMessage != null;
        this.plugin = plugin;
    }

    public AbstractCommand(@NotNull Plugin plugin, @NotNull String name) {
        super(name);
        this.plugin = plugin;
    }

    @Deprecated
    public AbstractCommand(@NotNull String name) {
        super(name);
        this.plugin = RegionsPlugin.getInstance();
    }

    public abstract boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args);

    @Nullable
    public abstract List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location);

    @Override
    public final boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        boolean success = false;

        if (!plugin.isEnabled()) {
            throw new CommandException("Cannot execute command '" + commandLabel + "' in plugin " + plugin.getDescription().getFullName() + " - plugin is disabled.");
        }

        if (!testPermission(sender)) {
            return true;
        }

        try {
            success = onCommand(sender, commandLabel, args);
        } catch (Throwable e) {
            throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin " + plugin.getDescription().getFullName(), e);
        }

        if (!success && usageMessage.length() > 0 && sendUsageMessage) {
            for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
                sender.sendMessage(line);
            }
        }

        return success;
    }

    @NotNull
    @Override
    public final List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) {
        List<String> completions = null;

        try {
            completions = onTabComplete(sender, alias, args, location);
        } catch (Throwable e) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');

            for (String arg : args) {
                message.append(arg).append(' ');
            }

            message.deleteCharAt(message.length() - 1).append("' in plugin ").append(plugin.getDescription().getFullName());

            throw new CommandException(message.toString(), e);
        }

        return (completions != null) ? completions : super.tabComplete(sender, alias, args);
    }

    @NotNull
    @Override
    public final List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return tabComplete(sender, alias, args, null);
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    public Command setAliases(@NotNull String... aliases) {
        return setAliases(Arrays.asList(aliases));
    }

    @Deprecated(forRemoval = true)
    @SneakyThrows
    public boolean isNumber(@NotNull String string, @NotNull Class<? extends Number> numberClass) {
        try {
            numberClass.getConstructor(numberClass).newInstance(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean testPermission(@NotNull CommandSender target, @Nullable String permission) {
        if (testPermissionSilent(target) || permission == null || permission.isBlank()) {
            return true;
        }

        Component permissionMessage = permissionMessage();
        if (permissionMessage == null) {
            permissionMessage = PaperComponents.legacySectionSerializer().deserialize(Bukkit.getPermissionMessage());
        }

        if (!permissionMessage.equals(Component.empty())) {
            target.sendMessage(permissionMessage.replaceText(net.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral("<permission>").replacement(permission).build()));
        }

        return false;
    }

    public boolean testPermissionSilent(@NotNull CommandSender target, @Nullable String permission) {
        if (permission == null || permission.isBlank()) {
            return true;
        }

        for (String p : permission.split(";")) {
            if (target.hasPermission(p)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean testPermission(@NotNull CommandSender target) {
        return testPermission(target, getPermission());
    }

    @Override
    public boolean testPermissionSilent(@NotNull CommandSender target) {
        return testPermissionSilent(target, getPermission());
    }

    @NotNull
    protected List<String> tabCompletePlayers(@Nullable String token, @NotNull BiPredicate<String, String> nameMatcher, @NotNull Collection<? extends Player> players) {
        List<String> playerNames = new ArrayList<>();

        if (token == null) {
            players.forEach(player -> playerNames.add(player.getName()));
            return playerNames;
        }

        players.forEach(player -> {
            String playerName = player.getName();
            if (nameMatcher.test(playerName, token)) {
                playerNames.add(playerName);
            }
        });

        return playerNames;
    }

    @NotNull
    protected List<String> tabCompletePlayers(@Nullable String token, @NotNull BiPredicate<String, String> nameMatcher) {
        return tabCompletePlayers(token, nameMatcher, Bukkit.getOnlinePlayers());
    }

    @NotNull
    protected List<String> tabCompletePlayers(@Nullable String token, @NotNull Collection<? extends Player> players) {
        return tabCompletePlayers(token, (name, localToken) -> name.toLowerCase().startsWith(localToken.toLowerCase()), players);
    }

    @NotNull
    protected List<String> tabCompletePlayers(@Nullable String token) {
        return tabCompletePlayers(token, (name, localToken) -> name.toLowerCase().startsWith(localToken.toLowerCase()), Bukkit.getOnlinePlayers());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(", ").append(plugin.getDescription().getFullName()).append(')');
        return stringBuilder.toString();
    }

}
