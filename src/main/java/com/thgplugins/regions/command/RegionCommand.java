package com.thgplugins.regions.command;

import com.google.common.collect.Lists;
import com.thgplugins.regions.controller.RegionController;
import com.thgplugins.regions.listener.RegionManageListener;
import com.thgplugins.regions.manager.InventoryManager;
import com.thgplugins.regions.provider.RegionManageProvider;
import com.thgplugins.regions.provider.RegionsProvider;
import com.thgplugins.regions.util.RegionsConstants;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RegionCommand extends AbstractCommand{
    public RegionCommand(@NotNull Plugin plugin) {
        super(plugin, "region");
        setAliases("regions", "rg");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player))
            return true;

        if (args.length == 0){
            InventoryManager.regionsProvider(new RegionsProvider()).open(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("wand")){
            player.getInventory().addItem(RegionsConstants.createWand().clone());
        } else if (args[0].equalsIgnoreCase("create")){
            if (!testPermissionSilent(player, RegionsConstants.PERMISSION_CREATE_REGION)){
                player.sendMessage(RegionsConstants.NO_PERMISSION_MESSAGE);
                return true;
            }

            if (args.length == 1){
                player.sendMessage("§cUse: §f/region create §7<name>");
                return true;
            }

            var name = args[1];

            if (name.length() > 32){
                player.sendMessage("§cPlease put a name that is shorter than 32 characters.");
                return true;
            }

            Optional.ofNullable(RegionManageListener.PLAYERS_MARKING_POSITION_MAP.get(player.getUniqueId())).ifPresentOrElse(mark -> {
                if (Objects.isNull(mark.getLoc1())){
                    player.sendMessage("§cYou need to set location 1.");
                    return;
                }
                if (Objects.isNull(mark.getLoc2())){
                    player.sendMessage("§cYou need to set location 2.");
                    return;
                }

                RegionController.getInstance().insert(player.getUniqueId(), mark, name).thenRun(() -> {
                    player.sendMessage(String.format("§aYou have successfully created the region §f%s§a.", name));
                });


            }, () -> player.sendMessage("§cYou need to set location 1."));
        }  else if (args[0].equalsIgnoreCase("add")){
            if (!testPermissionSilent(player, RegionsConstants.PERMISSION_ADD_REGION)){
                player.sendMessage(RegionsConstants.NO_PERMISSION_MESSAGE);
                return true;
            }

            if (args.length <= 2){
                player.sendMessage("§cUse: §f/region add §7<name> <player>");
                return true;
            }

            var name = args[1];
            var target = args[2];

            RegionController.getInstance().fetch(name).ifPresentOrElse(region -> {
                if (region.getWhitelist().contains(target)) {
                    player.sendMessage("§cThis player is already in the whitelist.");
                    return;
                }

                region.getWhitelist().add(target);
                region.update();

                player.sendMessage(String.format("§f%s §ahas been added to the whitelist of the region §f'%s'§a.", target, name));
            }, () -> player.sendMessage(RegionsConstants.REGION_NOT_FOUND));

        } else if (args[0].equalsIgnoreCase("remove")){
            if (!testPermissionSilent(player, RegionsConstants.PERMISSION_REMOVE_REGION)){
                player.sendMessage(RegionsConstants.NO_PERMISSION_MESSAGE);
                return true;
            }

            if (args.length <= 2){
                player.sendMessage("§cUse: §f/region remove §7<name> <player>");
                return true;
            }

            var name = args[1];
            var target = args[2];

            RegionController.getInstance().fetch(name).ifPresentOrElse(region -> {
                if (!region.getWhitelist().contains(target)) {
                    player.sendMessage("§cThis player is not in the whitelist.");
                    return;
                }

                region.getWhitelist().remove(target);
                region.update();

                player.sendMessage(String.format("§f%s §ahas been removed to the whitelist of the region §f'%s'§a.", target, name));
            }, () -> player.sendMessage(RegionsConstants.REGION_NOT_FOUND));

        } else if (args[0].equalsIgnoreCase("whitelist")){
            if (!testPermissionSilent(player, RegionsConstants.PERMISSION_WHITELIST_REGION)){
                player.sendMessage(RegionsConstants.NO_PERMISSION_MESSAGE);
                return true;
            }

            if (args.length == 1){
                player.sendMessage("§cUse: §f/region whitelist §7<name>");
                return true;
            }

            var name = args[1];

            RegionController.getInstance().fetch(name).ifPresentOrElse(region -> {
                player.sendMessage(String.format("§e§lWHITELIST: §f%s.", String.join(", ", region.getWhitelist())));
            }, () -> player.sendMessage(RegionsConstants.REGION_NOT_FOUND));

        } else {

            var regionName = args[0];

            RegionController.getInstance().fetch(regionName).ifPresent(region -> {
                InventoryManager.regionProvider(new RegionManageProvider(region)).open(player);
            });

        }



        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        if (args.length == 1){
            return StringUtil.copyPartialMatches(args[0], List.of("create", "wand", "add", "remove", "whitelist", "<name>"), Lists.newArrayList());
        }
        return Collections.emptyList();
    }
}
