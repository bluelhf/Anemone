package io.github.bluelhf.anemone.gui;

import io.github.bluelhf.anemone.Anemones;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    public abstract List<String> getTemplate();

    /**
     * Returns an item based on the index and view context.
     * @param index The index that the item is being generated for
     * @param context The context that the item is being generated in
     * @return An item based on the index and view context.
     * */
    public abstract ItemStack itemFor(Index index, ViewContext context);

    /**
     * Returns the title of the Anemone as a {@link Component}. May be null
     * @return The title of the Anemone or null if none is provided.
     * */
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
    protected void onClick(Index index, ViewContext context, InventoryClickEvent event) {
    }

    /**
     * Overridden by subclasses that wish to implement drag functionality.
     * @param indices The indices that were dragged on.
     * @param context The view context that the drag happened in.
     * @param event The actual drag event.
     * */
    protected void onDrag(ArrayList<Index> indices, ViewContext context, InventoryDragEvent event) {
    }

    /**
     * Overridden by subclasses that wish to implement open functionality.
     * @param context The view context that was created.
     * */
    protected void onOpen(ViewContext context) {
    }

    /**
     * Overridden by subclasses that wish to implement close functionality.
     * @param context The view context that is about to be closed.
     * */
    protected void onClose(ViewContext context) {
    }

    protected final Inventory getInventory(ViewContext context) {
        Inventory inventory = createInventory();
        int totalCounter = 0;
        HashMap<Character, Integer> charCounter = new HashMap<>();
        for (String s : getTemplate()) {
            for (char c : s.toCharArray()) {
                int totalIndex = getSize() * context.getPage() + totalCounter;
                int charIndex = charCounter.get(c);
                Index index = new Index(c, context.getPage(), charIndex, totalIndex);

                inventory.setItem(totalCounter, itemFor(index, context));

                charCounter.putIfAbsent(c, 0);
                charCounter.put(c, charCounter.get(c) + 1);
            }
        }

        return inventory;
    }

    private Inventory createInventory() {
        return getType() != null
                ? getTitle() != null
                    ? Bukkit.createInventory(null, getType(), getTitle())
                    : Bukkit.createInventory(null, getType())
                : getTitle() != null
                    ? Bukkit.createInventory(null, getSize(), getTitle())
                    : Bukkit.createInventory(null, getSize());
    }


    protected final InventoryType getType() {
        int[] size = getRowCol();
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

    protected final int getCount(char c) {
        List<String> template = getTemplate();
        String pattern = Pattern.quote("" + c);
        int count = 0;
        for (String s : template) count += s.split(pattern).length - 1;
        return count;
    }

    @Nullable
    protected final Character charFor(int rawSlot) {
        List<String> template = getTemplate();
        int ctr = 0;
        for (String s : template) {
            if (rawSlot - ctr > s.length()) {
                ctr += s.length();
                continue;
            }
            for (int i = 0; i < s.length(); i++) {
                if (rawSlot == ctr) return s.charAt(i);
                ctr++;
            }
        }

        return null;
    }

    protected final int charsUpTo(int rawSlot, char c) {
        int charCtr = 0;
        int total = 0;
        List<String> template = getTemplate();
        dance:
        for (String s : template) {
            for (char ch : s.toCharArray()) {
                if (c == ch) charCtr++;
                if (total == rawSlot) break dance;
                total++;
            }
        }

        return charCtr;
    }

    private int[] getRowCol() {
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

    public final int getSize() {
        return getRowCol()[0] * getRowCol()[1];
    }
}
