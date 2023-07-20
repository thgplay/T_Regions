package com.thgplugins.regions.manager;

import com.thgplugins.regions.RegionsPlugin;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryProvider;

public class InventoryManager {

    public static SmartInventory regionProvider(InventoryProvider provider){
        return SmartInventory.builder()
                .id("regionMenu")
                .title("§8Region")
                .manager(RegionsPlugin.getInventoryManager())
                .provider(provider)
                .size(4,9)
                .build();
    }

    public static SmartInventory regionWhitelistProvider(InventoryProvider provider){
        return SmartInventory.builder()
                .id("regionWhitelistAdd")
                .title("§8Whitelist")
                .manager(RegionsPlugin.getInventoryManager())
                .provider(provider)
                .size(5,9)
                .build();
    }

    public static SmartInventory regionsProvider(InventoryProvider provider){
        return SmartInventory.builder()
                .id("regionsProvider")
                .title("§8Regions")
                .manager(RegionsPlugin.getInventoryManager())
                .provider(provider)
                .size(6,9)
                .build();
    }

}
