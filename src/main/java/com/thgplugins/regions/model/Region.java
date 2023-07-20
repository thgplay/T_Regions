package com.thgplugins.regions.model;

import com.google.common.collect.Lists;
import com.thgplugins.regions.repository.RegionRepository;
import com.thgplugins.regions.util.RegionsConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Region {

    private long id;
    private String name;

    private int x1,x2,z1,z2;

    private UUID owner;
    private List<String> whitelist = Lists.newArrayList();

    @SneakyThrows
    public Region(ResultSet rs){
        this.id = rs.getLong("id");
        this.name = rs.getString("name");
        this.x1 = rs.getInt("x1");
        this.x2 = rs.getInt("x2");
        this.z1 = rs.getInt("z1");
        this.z2 = rs.getInt("z2");
        this.owner = UUID.fromString(rs.getString("owner"));
        Optional.ofNullable(rs.getString("whitelist")).ifPresent(s -> {
            if (!s.isEmpty())
                this.whitelist = Arrays.stream(s.split(",")).collect(Collectors.toCollection(ArrayList::new));
        });

    }

    public Region location(Location location1, Location location2){
        this.x1 = Math.min(location1.getBlockX(), location2.getBlockX());
        this.z1 = Math.min(location1.getBlockZ(), location2.getBlockZ());
        this.x2 = Math.max(location1.getBlockX(), location2.getBlockX());
        this.z2 = Math.max(location1.getBlockZ(), location2.getBlockZ());
        return this;
    }

    public Region location(int x1, int x2, int z1, int z2){
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.z2 = Math.max(z1, z2);
        return this;
    }

    public boolean contains(int x, int z) {
        return x >= x1 && x <= x2 && z >= z1 && z <= z2;
    }

    public boolean isAllowed(Player player){
        return this.whitelist.contains(player.getName()) || player.hasPermission(RegionsConstants.PERMISSION_BYPASS_REGION);
    }

    public void update(){
        RegionRepository.getInstance().update(this);
    }


}
