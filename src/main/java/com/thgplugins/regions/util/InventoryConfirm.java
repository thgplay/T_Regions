package com.thgplugins.regions.util;

import com.thgplugins.regions.RegionsPlugin;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.thgplugins.regions.util.ItemCreate.colorize;

public class InventoryConfirm {

    public static void open(@NotNull Player player, @NotNull String title, @Nullable ItemStack informationItemStack, @Nullable ItemStack confirmItemStack, @Nullable ItemStack cancelItemStack, @NotNull Runnable confirm, @NotNull Runnable cancel) {
        create(colorize(title), () ->
            new InventoryConfirmProvider(informationItemStack, confirmItemStack, cancelItemStack, confirm, cancel)
        ).open(player);
    }

    public static void open(@NotNull Player player, @Nullable ItemStack informationItemStack, @Nullable ItemStack confirmItemStack, @Nullable ItemStack cancelItemStack, @NotNull Runnable confirm, @NotNull Runnable cancel) {
        open(player, "§8Confirm", informationItemStack, confirmItemStack, cancelItemStack, confirm, cancel);
    }

    public static void open(@NotNull Player player, @Nullable String title, @Nullable ItemStack informationItemStack, @NotNull Runnable confirm, @NotNull Runnable cancel) {
        create(title, () ->
            new InventoryConfirmProvider(informationItemStack, confirm, cancel)
        ).open(player);
    }

    public static void open(@NotNull Player player, @Nullable ItemStack informationItemStack, @NotNull Runnable confirm, @NotNull Runnable cancel) {
        open(player, null, informationItemStack, confirm, cancel);
    }

    private static SmartInventory create(String title, Supplier<InventoryConfirmProvider> providerSupplier) {
        return SmartInventory.builder()
            .id("regions:confirm")
            .provider(providerSupplier.get())
            .size(3, 9)
            .title("§8Confirm")
            .manager(RegionsPlugin.getInventoryManager())
            .build();
    }

}
