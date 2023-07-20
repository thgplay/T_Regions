package com.thgplugins.regions.util;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryConfirmProvider implements InventoryProvider {

    private static final ItemStack DEFAULT_CONFIRM_ITEM_STACK = ItemCreate.create(Material.GREEN_TERRACOTTA)
            .name("&aConfirm!")
            .lore("&7Click to confirm this action.")
            .getItem();

    private static final ItemStack DEFAULT_CANCEL_ITEM_STACK = ItemCreate.create(Material.RED_TERRACOTTA)
            .name("&cCancel.")
            .lore("&7Click to cancel this action.")
            .getItem();

    private final ItemStack informationItemStack;
    private final ItemStack confirmItemStack, cancelItemStack;

    private final Runnable confirm;
    private final Runnable cancel;

    public InventoryConfirmProvider(@Nullable ItemStack informationItemStack, @Nullable ItemStack confirmItemStack, @Nullable ItemStack cancelItemStack, @NotNull Runnable confirm, @NotNull Runnable cancel) {
        this.informationItemStack = (informationItemStack != null) ? informationItemStack.clone() : null;
        this.confirmItemStack = (confirmItemStack != null) ? confirmItemStack.clone() : DEFAULT_CONFIRM_ITEM_STACK;
        this.cancelItemStack = (cancelItemStack != null) ? cancelItemStack.clone() : DEFAULT_CANCEL_ITEM_STACK;

        this.confirm = confirm;
        this.cancel = cancel;
    }

    public InventoryConfirmProvider(@Nullable ItemStack informationItemStack, @NotNull Runnable confirm, @NotNull Runnable cancel) {
        this(informationItemStack, DEFAULT_CONFIRM_ITEM_STACK, DEFAULT_CANCEL_ITEM_STACK, confirm, cancel);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(1, 1, ClickableItem.of(confirmItemStack, event -> {
            this.confirm.run();
            player.closeInventory();
        }));

        if (informationItemStack != null) {
            contents.set(0, 4, ClickableItem.empty(informationItemStack));
        }

        contents.set(1, 7, ClickableItem.of(cancelItemStack, event -> {
            this.cancel.run();
            player.closeInventory();
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
