package io.github.bluelhf.anemone.gui;

import io.github.bluelhf.anemone.Anemones;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

/**
 * ViewContext represents the context in which an Anemone subclass is being viewed.
 * @see Anemones#open(HumanEntity, Class)
 * */
public class ViewContext {
    private final Inventory inventory;
    private final HumanEntity viewer;
    private final Anemone host;
    private int page;

    public ViewContext(HumanEntity viewer, Anemone host) {
        this.viewer = viewer;
        this.host = host;
        this.page = 0;
        this.inventory = host.getInventory(this);
    }

    /**
     * Returns the {@link HumanEntity} that this ViewContext is for
     * @return The HumanEntity that this ViewContext is for
     * */
    public HumanEntity getViewer() {
        return viewer;
    }

    /**
     * Opens this ViewContext to the viewer if it is not already open.
     * */
    public void open() {
        if (!viewer.getOpenInventory().getTopInventory().equals(inventory)) {
            update();
            viewer.openInventory(inventory);
        }
    }

    /**
     * Updates the items in this ViewContext's inventory according to this ViewContext's {@link Anemone}
     * */
    public void update() {
        Inventory newInventory = host.getInventory(this);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, newInventory.getItem(i));
        }
    }

    /**
     * Increments this ViewContext's page, updating the display for the viewer
     * */
    public void next() {
        this.page++;
        update();
    }

    /**
     * Decrements this ViewContext's page, updating the display for the viewer
     * */
    public void previous() {
        this.page--;
        update();
    }

    /**
     * Resets this ViewContext's page, updating the display for the viewer
     * */
    public void reset() {
        this.page = 0;
        update();
    }

    /**
     * Returns the page that this ViewContext is on
     * @return The page that this ViewContext is on
     * */
    public int getPage() {
        return page;
    }

    /**
     * Sets this ViewContext's page, updating the display for the viewer
     * @param page The page to set the ViewContext's page to
     * */
    public void setPage(int page) {
        this.page = page;
        update();
    }

    /**
     * Returns the {@link Anemone} of this ViewContext
     * @return The {@link Anemone} of this ViewContext
     * */
    public Anemone getAnemone() {
        return host;
    }

    /**
     * Calls the click method of the host {@link Anemone}.
     * @param event The event to call the method with
     * @deprecated Internal use only.
     * */
    @Deprecated
    public void onClick(InventoryClickEvent event) {
        Index index = fromSlot(event.getRawSlot());
        if (index == null) return;

        host.onClick(index, this, event);
    }

    /**
     * Calls the drag method of the host {@link Anemone}.
     * @param event The event to call the method with
     * @deprecated Internal use only.
     * */
    @Deprecated
    public void onDrag(InventoryDragEvent event) {
        ArrayList<Index> indices = new ArrayList<>();
        for (int rawSlot : event.getRawSlots()) {
            Index index = fromSlot(rawSlot);
            if (index != null) indices.add(index);
        }

        host.onDrag(indices, this, event);
    }

    /**
     * Calls the close method of the host {@link Anemone}.
     * @deprecated Internal use only.
     * */
    @Deprecated
    public void onClose() {
        host.onClose(this);
    }

    /**
     * Calls the open method of the host {@link Anemone}.
     * @deprecated Internal use only.
     * */
    @Deprecated
    public void onOpen() {
        host.onOpen(this);
    }

    private Index fromSlot(int rawSlot) {
        Anemone anemone = host;
        Character ch = anemone.charFor(rawSlot);
        if (ch == null) return null;

        int perPage = anemone.getCount(ch);
        int charIndex = 0;
        charIndex += perPage * page;
        charIndex += anemone.charsUpTo(rawSlot, ch);
        int totalIndex = anemone.getSize() * rawSlot;
        return new Index(ch, page, charIndex, totalIndex);
    }

    protected Inventory getInventory() {
        return inventory;
    }
}
