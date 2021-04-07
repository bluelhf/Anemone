package io.github.bluelhf.anemone.gui;

import io.github.bluelhf.anemone.Anemones;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewContext represents the context in which an Anemone subclass is being viewed.
 *
 * @see Anemones#open(HumanEntity, Class)
 */
@SuppressWarnings("unused") // API
public class ViewContext {
    private final @NotNull Inventory inventory;
    private final HumanEntity viewer;
    private final @NotNull Anemone anemone;
    private int page;

    public ViewContext(HumanEntity viewer, @NotNull Anemone anemone) {
        this.viewer = viewer;
        this.anemone = anemone;
        this.page = 0;
        this.inventory = anemone.getInventory(this);
    }

    /**
     * Returns the {@link HumanEntity} that this ViewContext is for
     *
     * @return The HumanEntity that this ViewContext is for
     */
    public HumanEntity getViewer() {
        return viewer;
    }

    /**
     * Opens this ViewContext to the viewer if it is not already open.
     */
    public void open() {
        if (!viewer.getOpenInventory().getTopInventory().equals(inventory)) {
            update();
            viewer.openInventory(inventory);
        }
    }

    /**
     * Updates the items in this ViewContext's inventory according to this ViewContext's {@link Anemone}
     */
    public void update() {
        Inventory newInventory = anemone.getInventory(this);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, newInventory.getItem(i));
        }
    }

    /**
     * Increments this ViewContext's page, updating the display for the viewer
     */
    public void next() {
        this.page++;
        update();
    }

    /**
     * Decrements this ViewContext's page, updating the display for the viewer
     */
    public void previous() {
        this.page--;
        update();
    }

    /**
     * Resets this ViewContext's page, updating the display for the viewer
     */
    public void reset() {
        this.page = 0;
        update();
    }

    /**
     * Returns the page that this ViewContext is on
     *
     * @return The page that this ViewContext is on
     */
    public int getPage() {
        return page;
    }

    /**
     * Sets this ViewContext's page, updating the display for the viewer
     *
     * @param page The page to set the ViewContext's page to
     */
    public void setPage(int page) {
        this.page = page;
        update();
    }

    /**
     * Returns the {@link Anemone} of this ViewContext
     *
     * @return The {@link Anemone} of this ViewContext
     */
    public @NotNull Anemone getAnemone() {
        return anemone;
    }

    /**
     * Calls the click method of the host {@link Anemone}.
     * @param event The event to call the method with
     * @hidden Internal use only.
     */
    public void onClick(@NotNull InventoryClickEvent event) {
        Index index = anemone.fromSlot(page, event.getRawSlot());
        if (index == null) return;

        anemone.onClick(index, this, event);
    }

    /**
     * Calls the drag method of the host {@link Anemone}.
     *
     * @param event The event to call the method with
     * @hidden Internal use only.
     */
    public void onDrag(@NotNull InventoryDragEvent event) {
        List<Index> indices = new ArrayList<>();
        for (int rawSlot : event.getRawSlots()) {
            Index index = anemone.fromSlot(page, rawSlot);
            if (index != null) indices.add(index);
        }

        anemone.onDrag(indices, this, event);
    }

    /**
     * Calls the close method of the host {@link Anemone}.
     *
     * @hidden Internal use only.
     */
    public void onClose() {
        anemone.onClose(this);
    }

    /**
     * Calls the open method of the host {@link Anemone}.
     *
     * @hidden Internal use only.
     */
    public void onOpen() {
        anemone.onOpen(this);
    }
}
