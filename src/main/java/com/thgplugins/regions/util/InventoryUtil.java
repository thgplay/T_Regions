package com.thgplugins.regions.util;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class InventoryUtil {

    public static ClickableItem backItem(Player player, SmartInventory smartInventory) {
        return ClickableItem.of(ItemCreate.create(Material.TRIPWIRE_HOOK).name("§cBack").getItem(), e -> TaskUtil.runTask(() -> smartInventory.open(player)));
    }

}
