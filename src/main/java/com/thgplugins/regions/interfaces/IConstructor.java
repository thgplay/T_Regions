package com.thgplugins.regions.interfaces;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface IConstructor {

    void init();

    void initControllers();

    void initCommands();

    void initListeners();

    void initTasks();

    List<Command> COMMANDS = Lists.newArrayList();

    @Deprecated
    List<Command> cmds = COMMANDS;

    default void register(@NotNull JavaPlugin plugin, @NotNull Listener... listeners) {
        Stream.of(listeners).forEach($ -> plugin.getServer().getPluginManager().registerEvents($, plugin));
    }

    default void register(@NotNull Command... commands) {
        CommandMap commandMap = Bukkit.getCommandMap();

        Stream.of(commands).forEach(command -> {
            this.forciblyRegister(commandMap, "regions", command);
            COMMANDS.add(command);
        });
    }

    @Deprecated(forRemoval = true)
    default void register(@NotNull JavaPlugin plugin, @NotNull Command... commands) {
        this.register(commands);
    }

    default void unloadAll(@NotNull JavaPlugin plugin) {
        this.unregister(plugin, Iterables.toArray(COMMANDS, Command.class));
    }

    @SneakyThrows
    default void unregister(@NotNull JavaPlugin plugin, @NotNull Command... commands) {
        CommandMap commandMap = Bukkit.getCommandMap();
        Map<String, Command> knownCommands = commandMap.getKnownCommands();
        COMMANDS.removeAll(Arrays.asList(commands));

        Arrays.asList(commands).forEach(cmd -> {
            knownCommands.remove(cmd.getName());
            for (String alias : cmd.getAliases()) {
                if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(cmd.getName())) {
                    knownCommands.remove(alias);
                }
            }
        });
    }

    private void forciblyRegister(CommandMap commandMap, String fallbackPrefix, Command command) {
        Map<String, Command> knownCommands = commandMap.getKnownCommands();

        command.getAliases().forEach(knownCommands::remove);
        knownCommands.remove(command.getName());

        commandMap.register(fallbackPrefix, command);
    }

}
