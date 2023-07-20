package com.thgplugins.regions.listener;

import com.thgplugins.regions.controller.RegionController;
import com.thgplugins.regions.manager.InventoryManager;
import com.thgplugins.regions.model.Region;
import com.thgplugins.regions.provider.RegionManageProvider;
import com.thgplugins.regions.provider.RegionWhitelistManageProvider;
import com.thgplugins.regions.util.RegionsConstants;
import com.thgplugins.regions.util.TaskUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RegionManageListener implements Listener {

    public static final ExpiringMap<UUID, Region> PLAYER_ADD_WHITELIST = ExpiringMap.builder()
            .expiration(10L, TimeUnit.MINUTES)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    public static final ExpiringMap<UUID, Region> PLAYER_CHANGE_NAME = ExpiringMap.builder()
            .expiration(10L, TimeUnit.MINUTES)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    public static final ExpiringMap<UUID, Mark> PLAYERS_MARKING_POSITION_MAP = ExpiringMap.builder()
            .expiration(10L, TimeUnit.MINUTES)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    @EventHandler
    public void onInteract(PlayerInteractEvent e){

        var player = e.getPlayer();
        var item = e.getItem();
        var block = e.getClickedBlock();

        if (Objects.isNull(block) || Objects.isNull(item))
            return;



        if (!item.isSimilar(RegionsConstants.createWand()))
            return;

        Optional.ofNullable(PLAYERS_MARKING_POSITION_MAP.get(player.getUniqueId())).ifPresentOrElse(mark -> {

            e.setCancelled(true);
            var locationIsRegion = RegionController.getInstance().fetch(block.getLocation()).isPresent();
            if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {

                if (locationIsRegion){
                    player.sendMessage("§cThere is already a region where you mark.");
                    return;
                }

                mark.setLoc1(block.getLocation());
                player.sendMessage("§eYou have selected location 1.");

            } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

                if (locationIsRegion){
                    player.sendMessage("§cThere is already a region where you mark.");
                    return;
                }

                mark.setLoc2(block.getLocation());
                player.sendMessage("§eYou have selected location 2.");

            }

            var region = mark.getRegion();

            if (Objects.nonNull(region) && Objects.nonNull(mark.getLoc1()) && Objects.nonNull(mark.getLoc2())) {
                region.location(mark.getLoc1(), mark.getLoc2()).update();
                player.sendMessage(String.format("§f%s §eregion coordinates have been updated.", region.getName()));
                PLAYERS_MARKING_POSITION_MAP.remove(player.getUniqueId());
                TaskUtil.runTask(() -> InventoryManager.regionProvider(new RegionManageProvider(region)).open(player));
            }

        }, () -> {

        });



    }


    @AllArgsConstructor
    @Data
    public static class Mark {
        private Region region;
        private Location loc1, loc2;

        public Mark(Location loc1, Location loc2){
            this.loc1 = loc1;
            this.loc2 = loc2;
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){

        var player = e.getPlayer();

        Optional.ofNullable(PLAYER_ADD_WHITELIST.get(player.getUniqueId())).ifPresent(region -> {
            e.setCancelled(true);

            var message = ChatColor.stripColor(e.getMessage());

            if (!c(player, message))
                return;

            if (!message.equals("cancel")) {
                region.getWhitelist().add(message);
                region.update();
            }

            PLAYER_ADD_WHITELIST.remove(player.getUniqueId());

            TaskUtil.runTask(() -> InventoryManager.regionWhitelistProvider(new RegionWhitelistManageProvider(region, InventoryManager.regionProvider(new RegionManageProvider(region)))).open(player));

        });

        Optional.ofNullable(PLAYER_CHANGE_NAME.get(player.getUniqueId())).ifPresent(region -> {
            e.setCancelled(true);

            var message = e.getMessage();

            if (message.contains(" ")) {
                player.sendMessage("§cDo not use space.");
                return;
            }

            if (!message.equals("cancel")) {
                RegionController.getInstance().rename(region, message);
            }

            PLAYER_CHANGE_NAME.remove(player.getUniqueId());

            TaskUtil.runTask(() -> InventoryManager.regionProvider(new RegionManageProvider(region)).open(player));

        });

        Optional.ofNullable(PLAYERS_MARKING_POSITION_MAP.get(player.getUniqueId())).ifPresent(mark -> {

            var region = mark.getRegion();
            if (Objects.isNull(region))
                return;

            var message = ChatColor.stripColor(e.getMessage());

            if (message.equals("cancel")) {
                e.setCancelled(true);
                PLAYERS_MARKING_POSITION_MAP.remove(player.getUniqueId());
                TaskUtil.runTask(() -> InventoryManager.regionProvider(new RegionManageProvider(region)).open(player));
            }

        });


    }

    private boolean c(Player player, String message){

        if (message.length() > 16){
            player.sendMessage("§cPut a name shorter than 16 characters.");
            return false;
        }

        if (!isAlphanumeric(message)) {
            player.sendMessage("§cDo not use special characters.");
            return false;
        }

        if (message.contains(" ")) {
            player.sendMessage("§cDo not use space.");
            return false;
        }

        return true;
    }


    public static boolean isAlphanumeric(String str) {
        String regex = "^[a-zA-Z0-9]+$";
        return str.matches(regex);
    }

}
