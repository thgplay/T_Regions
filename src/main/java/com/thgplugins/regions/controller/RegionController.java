package com.thgplugins.regions.controller;

import com.thgplugins.regions.RegionsPlugin;
import com.thgplugins.regions.cache.RegionCache;
import com.thgplugins.regions.interfaces.IController;
import com.thgplugins.regions.listener.RegionManageListener;
import com.thgplugins.regions.model.Region;
import com.thgplugins.regions.repository.RegionRepository;
import com.thgplugins.regions.util.TaskUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RegionController implements IController {

    @Getter
    private static RegionController instance;

    private RegionRepository repository;

    private RegionCache cache = new RegionCache();

    @Override
    public void init() {
        instance = this;
    }

    @Override
    public void loadRepositories() {
        this.repository = new RegionRepository();
        this.repository.init();
       TaskUtil.runTask(1, () -> this.repository.fetchAll().thenAccept(regions -> {
           regions.forEach(region -> this.cache.put(region));
           RegionsPlugin.getInstance().getLogger().info(String.format("%s regions have been loaded.", regions.size()));
       }));
    }

    @Override
    public void unload() {
        this.cache.getRegions().clear();
        this.cache = null;
        this.repository.close();
    }

    public CompletableFuture<Void> insert(UUID owner, RegionManageListener.Mark mark, String name){
        var future = new CompletableFuture<Void>();
        var region = new Region();
        region.setOwner(owner);
        region.location(mark.getLoc1(), mark.getLoc2());
        region.setName(name);
        this.repository.insert(region).thenRun(() -> {
            this.cache.put(region);
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Void> delete(Region region){
        var future = new CompletableFuture<Void>();
        this.repository.delete(region).thenRun(() -> {
            this.cache.remove(region);
            future.complete(null);
        });
        return future;
    }


    @NotNull
    public Optional<Region> fetch(int x, int z){
        return cache.fetchByLocation(x, z);
    }

    @NotNull
    public Optional<Region> fetch(Location location){
        return cache.fetchByLocation(location.getBlockX(), location.getBlockZ());
    }
    @NotNull
    public Optional<Region> fetch(Entity entity){
        return fetch(entity.getLocation());
    }

    @NotNull
    public Optional<Region> fetch(String name){
        return this.cache.fetchByName(name);
    }

    public void rename(Region region, String newName){
        this.cache.rename(region, newName);
        region.setName(newName);
        region.update();
    }

    public List<Region> getRegions(){
        return this.cache.getRegions().values().stream().toList();
    }


}
