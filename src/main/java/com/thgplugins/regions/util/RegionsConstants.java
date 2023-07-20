package com.thgplugins.regions.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RegionsConstants {

    public static final String database = "thg";

    public static final String PERMISSION_CREATE_REGION = "region.create";
    public static final String PERMISSION_MENU_REGION = "region.menu";
    public static final String PERMISSION_ADD_REGION = "region.add";
    public static final String PERMISSION_REMOVE_REGION = "region.remove";
    public static final String PERMISSION_WHITELIST_REGION = "region.whitelist";
    public static final String PERMISSION_BYPASS_REGION = "region.bypass";

    public static final String NO_PERMISSION_MESSAGE = "§cYou don't have sufficient permissions to do that.";
    public static final String REGION_NOT_FOUND = "§cNo region was found.";




    public static ItemStack createWand(){
        var item = new ItemStack(Material.STICK);
        var meta = item.getItemMeta();
        meta.setDisplayName("§bPosition Marker");
        meta.setLore(List.of("§fUse this item to mark a region.",
                "§eLeft Click - §bLocation §f1§b.",
                "§eRight Click - §bLocation §f2§b."));
        item.setItemMeta(meta);
        return item;
    }


}
