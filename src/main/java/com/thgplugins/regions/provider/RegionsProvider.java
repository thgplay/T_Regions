package com.thgplugins.regions.provider;

import com.thgplugins.regions.controller.RegionController;
import com.thgplugins.regions.manager.InventoryManager;
import com.thgplugins.regions.model.Region;
import com.thgplugins.regions.util.ItemCreate;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RegionsProvider implements InventoryProvider {

    private boolean forceUpdate;

    private final Region region;

    public RegionsProvider(Region region){
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

        var pagination = contents.pagination();
        pagination.setItemsPerPage(28);
        var clickableItems = RegionController.getInstance().getRegions().stream().map(region1 -> createRegionItem(player, region1)).toArray(ClickableItem[]::new);
        pagination.setItems(clickableItems);

        int row = 1, column = 1;
        for (ClickableItem pageItem : pagination.getPageItems()) {

            contents.set(row, column, pageItem);

            if (column == 7){
                row++;
                column = 1;
            } column++;

        }


        if (!pagination.isLast()){
            contents.set(5,3, ClickableItem.of(ItemCreate.create(Material.ARROW).name("§eBack").getItem(), e -> InventoryManager.regionsProvider(this).open(player, pagination.getPage() - 1)));
        }

        if (!pagination.isFirst()){
            contents.set(5,3, ClickableItem.of(ItemCreate.create(Material.ARROW).name("§eNext").getItem(), e -> InventoryManager.regionsProvider(this).open(player, pagination.getPage() + 1)));
        }




    }


    private ClickableItem createRegionItem(Player player, Region region){
        var builder = ItemCreate.create(Material.GRASS_BLOCK);
        builder.name(String.format("§b%s", region.getName()));
        builder.lore("§7§oClick §eto edit.");
        return ClickableItem.of(builder.getItem(), e -> InventoryManager.regionProvider(new RegionManageProvider(region)).open(player));
    }


}
