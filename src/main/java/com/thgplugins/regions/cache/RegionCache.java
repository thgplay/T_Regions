package com.thgplugins.regions.cache;

import com.google.common.collect.Maps;
import com.thgplugins.regions.model.Region;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor
public final class RegionCache {


    @Getter
    private final Map<Long, Region> regions = Maps.newHashMap();

    @Getter
    private final Map<String, Region> regionsByName = Maps.newHashMap();

    public void put(Region region){
        this.regions.putIfAbsent(region.getId(), region);
        this.regionsByName.putIfAbsent(region.getName().toLowerCase(), region);
    }

    public void remove(Region region){
        this.regions.remove(region.getId());
        this.regionsByName.remove(region.getName().toLowerCase());
    }

    public void rename(Region region, String newName){
        this.regionsByName.remove(region.getName().toLowerCase());
        this.regionsByName.putIfAbsent(newName.toLowerCase(), region);
    }

    public Optional<Region> fetchByName(String name){
        return Optional.ofNullable(this.regionsByName.get(name.toLowerCase()));
    }
    public Optional<Region> fetchByLocation(Location location){
        return fetchByLocation(location.getBlockX(), location.getBlockZ());
    }

    public Optional<Region> fetchByLocation(int x, int z){
        return regions.values().stream().filter(d -> d.contains(x,z)).findFirst();
    }


}
