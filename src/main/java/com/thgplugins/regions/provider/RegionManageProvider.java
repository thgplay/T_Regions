package com.thgplugins.regions.provider;

import com.thgplugins.regions.listener.RegionManageListener;
import com.thgplugins.regions.manager.InventoryManager;
import com.thgplugins.regions.model.Region;
import com.thgplugins.regions.util.ItemCreate;
import com.thgplugins.regions.util.RegionsConstants;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RegionManageProvider implements InventoryProvider {

    private boolean forceUpdate;

    private final Region region;

    public RegionManageProvider(Region region){
        this.region = region;
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        build(player, contents);

    }

    @Override
    public void update(Player player, InventoryContents contents) {

        if (!this.forceUpdate)
            return;

        this.forceUpdate = false;

        build(player, contents);

    }

    private void build(Player player, InventoryContents contents){

        contents.set(0,4, createRegionItem());
        contents.set(2,2, renameRegionItem(player));
        contents.set(2,4, addWhiteListItem(player));
        contents.set(2,6, updateLocationItem(player));

    }


    private ClickableItem createRegionItem(){
        var builder = ItemCreate.create(Material.GRASS_BLOCK);
        builder.name(String.format("§b%s", region.getName()));

        return ClickableItem.empty(builder.getItem());
    }

    private ClickableItem renameRegionItem(Player player){
        var builder = ItemCreate.create(Material.PAPER);
        builder.name("§eRename Region");
        builder.lore("§7§oClick §eto change.");
        return ClickableItem.of(builder.getItem(), e -> {

            RegionManageListener.PLAYER_CHANGE_NAME.putIfAbsent(player.getUniqueId(), this.region);
            player.closeInventory();

            player.sendMessage("","","§ePut the new region name.", "§7to cancel digite §c'cancel'§7.");
        });

    }

    private ClickableItem addWhiteListItem(Player player){
        var builder = ItemCreate.create(Material.BOOK);
        builder.name("§eWhitelist");
        builder.lore("§7§oClick §eto edit.");
        return ClickableItem.of(builder.getItem(), e -> InventoryManager.regionWhitelistProvider(new RegionWhitelistManageProvider(region, InventoryManager.regionProvider(this))).open(player));

    }


    private ClickableItem updateLocationItem(Player player){
        var builder = ItemCreate.create(Material.REDSTONE_TORCH);
        builder.name("§bUpdate Location");
        builder.lore("",
                " §eCurrent:",
                String.format("  §7x: §f%s §8| §7z: §f%s", region.getX1(), region.getZ1()),
                String.format("  §7x: §f%s §8| §7z: §f%s", region.getX2(), region.getZ2()),
                "",
                "§7§oClick §eto update."
                );
        return ClickableItem.of(builder.getItem(), e -> {


            RegionManageListener.PLAYERS_MARKING_POSITION_MAP.putIfAbsent(player.getUniqueId(), new RegionManageListener.Mark(region,null, null));
            player.closeInventory();
            player.getInventory().addItem(RegionsConstants.createWand());

            player.sendMessage("","","§eMark the new area of your region.","§7Left Click - §fMark Location 1.","§7Right Click - §fMark Location 2.", "§7to cancel digite §c'cancel'§7.");

        });

    }

}
