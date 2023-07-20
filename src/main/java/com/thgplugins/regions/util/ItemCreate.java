package com.thgplugins.regions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class ItemCreate {

    @Language("RegExp")
    private static final String COLOR_REGEX = "([&§](#[0-9a-fA-Fk-oK-O]{6}|[0-9a-fA-Fk-oK-O]))+";
    private static final Pattern COLOR_PATTERN = Pattern.compile(COLOR_REGEX);
    private static final NamespacedKey RANDOM_MODIFIER_KEY = new NamespacedKey("commons", "random_modifier");
    private static final LegacyComponentSerializer LEGACY_AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private static final Pattern HEX_PATTERN = Pattern.compile("&(#[A-Fa-f0-9]{6})");
    private ItemStack item;
    private ItemMeta meta;

    public ItemCreate(@NotNull ItemStack item) {
        this.item = item;
    }

    public ItemCreate() {
        this.item = new ItemStack(Material.AIR, 1);
    }

    public ItemCreate(@NotNull Material material) {
        this.item = new ItemStack(material, 1);
    }

    private ItemCreate(@NotNull ItemStack item, @Nullable ItemMeta meta) {
        this.item = item;
        this.meta = meta;
    }

    @NotNull
    public static ItemCreate create(@NotNull Material material) {
        return new ItemCreate(material);
    }

    @NotNull
    public static ItemCreate create(@NotNull Material material, int amount) {
        return new ItemCreate(new ItemStack(material, amount));
    }

    @NotNull
    public static ItemCreate create(@NotNull ItemStack itemStack) {
        return new ItemCreate(itemStack);
    }



    @NotNull
    public ItemCreate setItem(@NotNull ItemStack itemStack) {
        this.item = itemStack;
        return this;
    }

    @NotNull
    public ItemCreate setMeta(@NotNull ItemMeta itemMeta) {
        this.meta = itemMeta;
        return this;
    }

    @NotNull
    public ItemStack getItem() {
        if (meta != null) {
            this.item.setItemMeta(meta);
        }

        AtomicReference<@NotNull ItemStack> itemStack = new AtomicReference<>(this.item.clone());
        return item.clone();
    }

    @NotNull
    public ItemStack getGlow() {
        return glow().getItem();
    }

    @NotNull
    public ItemCreate hideAttributes() {
        this.getItemMeta().addItemFlags(ItemFlag.values());
        return this;
    }
    @NotNull
    public ItemCreate glow() {
        ItemMeta meta = getItemMeta();

        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        return this;
    }

    @NotNull
    public ItemCreate type(@NotNull Material material) {
        this.item.setType(material);
        return this;
    }

    @NotNull
    public ItemCreate amount(int amount, boolean bound) {
        if (bound)
            amount = Math.max(0, Math.min(amount, item.getMaxStackSize()));
        this.item.setAmount(amount);
        return this;
    }

    @NotNull
    public ItemCreate amount(int amount) {
        return amount(amount, true);
    }


    @NotNull
    public ItemCreate name(@Nullable Component component) {
        this.getItemMeta().displayName(component);
        return this;
    }

    @NotNull
    public ItemCreate name(@Nullable String name) {
        this.getItemMeta().setDisplayName(name != null ? colorize(name) : null);
        return this;
    }

    @NotNull
    public static String colorize(@NotNull String string) {
        Matcher matcher = HEX_PATTERN.matcher(ChatColor.translateAlternateColorCodes('&', string));
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            try {
                matcher.appendReplacement(result, net.md_5.bungee.api.ChatColor.of(matcher.group(1)).toString());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        return matcher.appendTail(result).toString();
    }

    @NotNull
    public ItemCreate lore() {
        this.getItemMeta().setLore(null);
        return this;
    }

    @NotNull
    public ItemCreate lore(@NotNull String @NotNull ... lore) {
        return lore(true, lore);
    }

    @NotNull
    public ItemCreate lore(boolean clear, @NotNull String @NotNull ... lore) {
        ItemMeta meta = getItemMeta();
        List<String> itemLore = new ArrayList<>();

        // If the item already has lore and we're not clearing lore, add it
        List<String> existingLore = meta.getLore();
        if (!clear && existingLore != null) {
            itemLore.addAll(existingLore);
        }

        for (String inputLine : lore) {
            if (!doesStringStartWithColorCode(inputLine)) {
                inputLine = "&7" + inputLine;
            }

            itemLore.add(colorize(inputLine));
        }

        meta.setLore(itemLore);
        return this;
    }

    @NotNull
    public ItemCreate lore(int index, @NotNull String line) {
        ItemMeta meta = getItemMeta();
        if (!meta.hasLore()) {
            return this;
        }

        List<String> itemLore = Objects.requireNonNull(meta).getLore();
        if (itemLore == null || index < 0 || index >= itemLore.size()) {
            return this;
        }

        if (line != null && !doesStringStartWithColorCode(line)) {
            line = "&7" + line;
        }

        itemLore.set(index, line != null ? colorize(line) : null);

        meta.setLore(itemLore);
        return this;
    }

    @NotNull
    public <T extends ItemMeta> ItemCreate meta(@NotNull Class<T> metaClass, @NotNull Consumer<T> metaApplier) {
        ItemMeta meta = getItemMeta();

        if (!metaClass.isInstance(meta)) {
            throw new IllegalArgumentException(
                    "Cannot apply meta of type " + metaClass.getName() + " to meta of type " + meta.getClass().getName());
        }

        metaApplier.accept(metaClass.cast(meta));

        return this;
    }

    @NotNull
    public ItemCreate model(int data) {
        try {
            this.getItemMeta().setCustomModelData((data != 0) ? data : null);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return this;
    }

    @NotNull
    private ItemMeta getItemMeta() {
        if (meta == null) {
            this.meta = item.getItemMeta();

            if (meta == null) {
                throw new IllegalStateException("Cannot operate on item if type is " + item.getType());
            }
        }

        return meta;
    }

    private boolean doesStringStartWithColorCode(@NotNull String string) {
        if (string.isEmpty()) {
            return false;
        }

        char firstChar = string.charAt(0);
        return firstChar == '&' || firstChar == ChatColor.COLOR_CHAR;
    }

}