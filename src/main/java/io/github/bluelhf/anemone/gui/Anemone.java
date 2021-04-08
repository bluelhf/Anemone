package io.github.bluelhf.anemone.gui;

import io.github.bluelhf.anemone.Anemones;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Abstract base class for all Anemones.
 * An anemone is a class that is registered via {@link Anemones} and creates dynamic inventories
 * based on a given template and an index.
 * */
public abstract class Anemone {

    /**
     * Returns the template to use.
     * @return The template to use.
     * */
    public abstract @NotNull List<String> getTemplate();

    /**
     * Returns an item based on the index and view context.
     * @param index The index that the item is being generated for
     * @param context The context that the item is being generated in
     * @return An item based on the index and view context.
     * */
    public abstract @NotNull ItemStack itemFor(Index index, ViewContext context);

    /**
     * Returns the title of the Anemone as a {@link Component}. May be null
     * @return The title of the Anemone or null if none is provided.
     * */
    @SuppressWarnings("SameReturnValue") // External subclasses will change
    @Nullable
    public Component getTitle() {
        return null;
    }

    /**
     * Overridden by subclasses that wish to implement click functionality.
     * @param index The index that was clicked on.
     * @param context The view context that the click happened in.
     * @param event The actual click event.
     * */
    @SuppressWarnings({"unused", "EmptyMethod"}) // External subclasses will implement
    protected void onClick(Index index, ViewContext context, InventoryClickEvent event) {
    }

    /**
     * Overridden by subclasses that wish to implement drag functionality.
     * @param indices The indices that were dragged on.
     * @param context The view context that the drag happened in.
     * @param event The actual drag event.
     * */
    @SuppressWarnings({"unused", "EmptyMethod"}) // External subclasses will implement
    protected void onDrag(List<Index> indices, ViewContext context, InventoryDragEvent event) {
    }

    /**
     * Overridden by subclasses that wish to implement open functionality.
     * @param context The view context that was created.
     * */
    @SuppressWarnings({"unused", "EmptyMethod"}) // External subclasses will implement
    protected void onOpen(ViewContext context) {
    }

    /**
     * Overridden by subclasses that wish to implement close functionality.
     * @param context The view context that is about to be closed.
     * */
    @SuppressWarnings({"unused", "EmptyMethod"}) // External subclasses will implement
    protected void onClose(ViewContext context) {
    }

    /**
     * Creates and fills an inventory with this Anemone's items in the given ViewContext
     * @param context The context to create the items for
     * @return The created and filled inventory
     * @hidden Internal use only
     * */
    protected final @NotNull Inventory getInventory(@NotNull ViewContext context) {
        Inventory inventory = createInventory();
        int totalCounter = 0;
        HashMap<Character, Integer> charCounter = new HashMap<>();
        for (String s : getTemplate()) {
            for (char c : s.toCharArray()) {
                charCounter.putIfAbsent(c, 0);
                int totalIndex = getSize() * context.getPage() + totalCounter;
                int charIndex = charCounter.get(c);
                Index index = new Index(c, context.getPage(), charIndex, totalIndex);

                inventory.setItem(totalCounter, itemFor(index, context));

                charCounter.put(c, charCounter.get(c) + 1);
                totalCounter++;
            }
        }

        return inventory;
    }

    /**
     * Creates an empty inventory for this Anemone
     * @return The created inventory
     * @hidden Internal use only.
     * */
    private @NotNull Inventory createInventory() {
        return getType() != null
                ? getTitle() != null
                    ? Bukkit.createInventory(null, getType(), getTitle())
                    : Bukkit.createInventory(null, getType())
                : getTitle() != null
                    ? Bukkit.createInventory(null, getSize(), getTitle())
                    : Bukkit.createInventory(null, getSize());
    }

    /**
     * Creates an index that represents the given slot of this Anemone on the given page
     * @param page The page that the index should be on
     * @param slot The slot to create the index for
     * @return The created index
     * @hidden Internal use only.
     * */
    protected @Nullable Index fromSlot(int page, int slot) {
        Character ch = charFor(slot);
        if (ch == null) return null;

        int perPage = getCount(ch);
        int charIndex = perPage * page;
        charIndex += charsUpTo(slot, ch);
        int totalIndex = getSize() * slot;
        return new Index(ch, page, charIndex, totalIndex);
    }

    /**
     * Returns the type that this inventory will use, or null if it has a custom size
     * @return The type that this inventory will use, or null if it has a custom size
     * @throws IllegalStateException If the template has no valid type
     * */
    public final @Nullable InventoryType getType() {
        int[] size = getColRow();
        if (size[0] == 3 && size[1] == 3) {
            return InventoryType.DISPENSER;
        } else if (size[0] == 9 && (size[1] == 3 || size[1] == 6)) {
            return InventoryType.CHEST;
        } else if (size[0] == 9 && size[1] <= 6) {
            return null;
        } else {
            throw new IllegalStateException("No valid type exists for given template bounds.");
        }
    }

    /**
     * Counts how many times the given character appears in this Anemone's template
     * @param c The character to count.
     * @return How many times the given character appears in this Anemone's template
     * */
    public final int getCount(char c) {
        List<String> template = getTemplate();
        String pattern = Pattern.quote("" + c);
        int count = 0;
        for (String s : template) {
            for (char b : s.toCharArray()) {
                if (b == c) count++;
            }
        }
        return count;
    }

    /**
     * Returns the character at the given slot in this Anemone's template
     * @param slot The slot
     * @return The character at the slot, or null if the slot exceeds the template size.
     * */
    @Nullable
    public final Character charFor(int slot) {
        List<String> template = getTemplate();
        int ctr = 0;
        for (String s : template) {
            if (slot - ctr > s.length()) {
                ctr += s.length();
                continue;
            }
            for (int i = 0; i < s.length(); i++) {
                if (slot == ctr) return s.charAt(i);
                ctr++;
            }
        }

        return null;
    }

    /**
     * Counts how many times the given character appears in this Anemone's template before the given slot.
     * @param rawSlot The slot to count up to
     * @param c The character to look for
     * @return How many times the given character appears in this Anemone's template before the given slot.
     * @hidden Internal use only.
     * */
    protected final int charsUpTo(int rawSlot, char c) {
        int charCtr = 0;
        int total = 0;
        List<String> template = getTemplate();
        dance:
        for (String s : template) {
            for (char ch : s.toCharArray()) {
                if (total == rawSlot) break dance;
                total++;

                if (c == ch) charCtr++;
            }
        }

        return charCtr;
    }

    /**
     * Calculates the row and column counts for this Anemone's template
     * @return An integer array, where [0] is the column count, and [1] is the row count.
     * @hidden Internal use only.
     * */
    private int @NotNull [] getColRow() {
        List<String> template = getTemplate();
        if (template.size() == 0) return new int[]{0, 0};

        int maxWidth = Integer.MIN_VALUE;
        for (String s : template) {
            if (s.length() > maxWidth) {
                maxWidth = s.length();
            }
        }

        return new int[]{
                maxWidth,
                template.size()
        };
    }

    /**
     * Returns the size of this Anemone's template in slots
     * @return The size of this Anemone's template in slots
     * */
    public final int getSize() {
        return getColRow()[0] * getColRow()[1];
    }
}
