package io.github.bluelhf.anemone.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Items is an utility class used to create {@link ItemStack}s.
 * It takes an ItemStack supplier and some stack or meta modifiers, and builds an ItemStack
 * by getting the stack from the supplier and applying the given modifiers.
 * */
@SuppressWarnings("unused") // API
public class Items {
    private Supplier<ItemStack> stackProvider;
    private final Collection<Consumer<ItemStack>> stackModifiers = new ArrayList<>();
    private final Collection<Consumer<ItemMeta>> metaModifiers = new ArrayList<>();

    private Items() {
    }

    /**
     * Creates a new Items with the given material and a count of 1.
     * @param material The material to use
     * @return The created Items
     * @see Items#of(Material, int)
     * @see Items#of(ItemStack)
     * @see Items#of(Supplier)
     * */
    public static @NotNull Items of(@NotNull Material material) {
        return of(material, 1);
    }

    /**
     * Creates a new Items with the given material and count.
     * @param material The material to use
     * @param count The item amount to use
     * @return The created Items
     * @see Items#of(Material)
     * @see Items#of(ItemStack)
     * @see Items#of(Supplier)
     * */
    public static @NotNull Items of(@NotNull Material material, int count) {
        return of(new ItemStack(material, count));
    }

    /**
     * Creates a new Items with the given {@link ItemStack}
     * @param stack The ItemStack to use
     * @return The created Items
     * @see Items#of(Material)
     * @see Items#of(Material, int)
     * @see Items#of(Supplier)
     * */
    public static @NotNull Items of(ItemStack stack) {
        return of(() -> stack);
    }

    /**
     * Creates a new Items with the given {@link Supplier}
     * @param stackProvider The supplier to provide the {@link ItemStack}s
     * @return The created Items
     * @see Items#of(Material)
     * @see Items#of(Material, int)
     * @see Items#of(ItemStack)
     * */
    public static @NotNull Items of(Supplier<ItemStack> stackProvider) {
        Items builder = new Items();
        builder.stackProvider = stackProvider;
        return builder;
    }


    /**
     * Sets the generated {@link ItemStack} to be modified by the given {@link Consumer}&lt;{@link ItemStack}&gt;
     * @param stackConsumer The consumer that modifies the stack
     * @return This Items
     * @see Items#modifyMeta(Consumer)
     * @see Items#build()
     * */
    public @NotNull Items modifyStack(Consumer<ItemStack> stackConsumer) {
        stackModifiers.add(stackConsumer);
        return this;
    }

    /**
     * Sets the generated {@link ItemStack} to be modified by the given {@link Consumer}&lt;{@link ItemMeta}&gt;
     * @param metaConsumer The consumer that modifies the item meta
     * @return This Items
     * @see Items#modifyStack(Consumer)
     * @see Items#build()
     * */
    public @NotNull Items modifyMeta(Consumer<ItemMeta> metaConsumer) {
        metaModifiers.add(metaConsumer);
        return this;
    }

    /**
     * Builds an {@link ItemStack} out of this Items by getting the stack from the stack supplier and applying the given modifiers.
     * @return The built ItemStack
     * @see Items#of(Supplier)
     * @see Items#modifyStack(Consumer)
     * @see Items#modifyMeta(Consumer)
     * */
    public ItemStack build() {
        ItemStack item = stackProvider.get();
        for (Consumer<ItemStack> stackModifier : stackModifiers) {
            stackModifier.accept(item);
        }
        for (Consumer<ItemMeta> metaModifier : metaModifiers) {
            ItemMeta meta = item.getItemMeta();
            metaModifier.accept(meta);
            item.setItemMeta(meta);
        }

        return item;
    }
}
