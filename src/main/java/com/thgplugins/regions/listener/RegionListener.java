package com.thgplugins.regions.listener;

import com.thgplugins.regions.controller.RegionController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;

import java.util.Objects;

public class RegionListener implements Listener {

    private final RegionController controller;

    public RegionListener(){
        this.controller = RegionController.getInstance();
    }

//    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
//    public void onInteract(PlayerInteractEvent event){
//
//        var player = event.getPlayer();
//        var block = event.getClickedBlock();
//        if (Objects.nonNull(block)) {
//            if (controller.fetch(block.getLocation()).stream().anyMatch(region -> region.isAllowed(player))) {
//                event.setCancelled(true);
//            }
//        }
//
//    }

    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(BlockBreakEvent event){

        var player = event.getPlayer();
        var location = event.getBlock().getLocation();
        controller.fetch(location).ifPresent(region -> {
            if (!region.isAllowed(player)) {
                event.setCancelled(true);
            }
        });

    }


    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(BlockPlaceEvent event){

        var player = event.getPlayer();
        var location = event.getBlockPlaced().getLocation();

        controller.fetch(location).ifPresent(region -> {
            if (!region.isAllowed(player)) {
                event.setCancelled(true);
            }
        });

    }

    @EventHandler(priority = EventPriority.LOW)
    private void onShearEntity(PlayerShearEntityEvent event) {
        var player = event.getPlayer();
        var entity = event.getEntity();
        controller.fetch(entity).ifPresent(region -> {
            if (!region.isAllowed(player)) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onLeashEntity(PlayerLeashEntityEvent event) {
        var player = event.getPlayer();
        var entity = event.getEntity();

        controller.fetch(entity).ifPresent(region -> {
            if (!region.isAllowed(player)) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPickupItem(EntityPickupItemEvent event) {

        if (!(event.getEntity() instanceof Player player))
            return;

        controller.fetch(event.getItem()).ifPresent(region -> {
            if (!region.isAllowed(player)) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onDropItem(PlayerDropItemEvent event) {

        var player = event.getPlayer();

        controller.fetch(player).ifPresent(region -> {
            if (!region.isAllowed(player)) {
                event.setCancelled(true);
            }
        });
    }

}
