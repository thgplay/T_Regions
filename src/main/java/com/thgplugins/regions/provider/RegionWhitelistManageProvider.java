package com.thgplugins.regions.provider;

import com.thgplugins.regions.listener.RegionManageListener;
import com.thgplugins.regions.manager.InventoryManager;
import com.thgplugins.regions.model.Region;
import com.thgplugins.regions.util.InventoryConfirm;
import com.thgplugins.regions.util.ItemCreate;
import com.thgplugins.regions.util.TaskUtil;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Objects;

import static com.thgplugins.regions.util.InventoryUtil.backItem;

public class RegionWhitelistManageProvider implements InventoryProvider {


    private final Region region;

    private final long UPDATE_TICK_RATE = 35;
    private long tick = 1;
    private boolean forceUpdate;

    private SmartInventory parent;

    public RegionWhitelistManageProvider(Region region, SmartInventory parent){
        this.region = region;
        this.parent = parent;
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        build(player, contents);

    }

    @Override
    public void update(Player player, InventoryContents contents) {

        if ((tick++ % UPDATE_TICK_RATE != 0) && (!forceUpdate))
            return;

        this.forceUpdate = false;

        build(player, contents);

    }

    private void build(Player player, InventoryContents contents){

        contents.set(0,4, addWhitelist(player));

        var pagination = contents.pagination();
        pagination.setItemsPerPage(15);

        var skulls = this.region.getWhitelist().stream().map(s -> createPlayerItem(player, s)).toArray(ClickableItem[]::new);
        pagination.setItems(skulls);

        if (skulls.length == 0){
            contents.set(2,4, ClickableItem.empty(ItemCreate.create(Material.COBWEB).name("§cEmpty.").getItem()));
        } else {

            int column = 2, row = 1;
            for (ClickableItem skull : pagination.getPageItems()) {

                contents.set(row, column, skull);

                if (column == 6) {
                    row++;
                    column = 2;
                } else column++;
            }
        }

        if (Objects.nonNull(this.parent))
            contents.set(4,4, backItem(player, this.parent));

        if (!pagination.isLast()){
            contents.set(4,3, ClickableItem.of(ItemCreate.create(Material.ARROW).name("§eBack").getItem(), e -> InventoryManager.regionWhitelistProvider(this).open(player, pagination.getPage() - 1)));
        }

        if (!pagination.isFirst()){
            contents.set(4,3, ClickableItem.of(ItemCreate.create(Material.ARROW).name("§eNext").getItem(), e -> InventoryManager.regionWhitelistProvider(this).open(player, pagination.getPage() + 1)));
        }


    }

    private ClickableItem createPlayerItem(Player player, String name){

        var item = new ItemStack(Material.PLAYER_HEAD);
        var meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(name);
        item.setItemMeta(meta);


        var builder = ItemCreate.create(item);
        builder.name(String.format("§e%s", name));
        builder.lore("§e§oLeft Click §7to remove.");

        return ClickableItem.of(builder.getItem(), e -> {
            InventoryConfirm.open(player, ItemCreate.create(builder.getItem().clone()).name(String.format("§cDo you want to remove §f%s§c?", name)).getItem(), () -> {
                region.getWhitelist().remove(name);
                region.update();
                TaskUtil.runTask(() -> InventoryManager.regionWhitelistProvider(this).open(player));
            }, () -> TaskUtil.runTask(() -> InventoryManager.regionWhitelistProvider(this).open(player)));

        });


    }

    private ClickableItem addWhitelist(Player player){
        var builder = ItemCreate.create(Material.PAPER);
        builder.name("§eAdd Whitelist");
        builder.lore("§e§oLeft Click §7to add.");

        return ClickableItem.of(builder.getItem(), e -> {

            RegionManageListener.PLAYER_ADD_WHITELIST.putIfAbsent(player.getUniqueId(), this.region);
            player.closeInventory();

            player.sendMessage("","","§ePut the name of the player you want to add to the region.", "§7to cancel digite §c'cancel'§7.");

        });
    }



}
