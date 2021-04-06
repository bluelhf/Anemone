package io.github.bluelhf.anemone.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class ViewContext {
    private final Inventory inventory;
    private final HumanEntity viewer;
    private final Anemone host;
    private int page;

    public ViewContext(Inventory inventory, HumanEntity viewer, Anemone host, int page) {
        this.inventory = inventory;
        this.viewer = viewer;
        this.host = host;
        this.page = page;
    }

    public ViewContext(HumanEntity viewer, Anemone host) {
        this.viewer = viewer;
        this.host = host;
        this.page = 0;
        this.inventory = host.getInventory(this);
    }

    public HumanEntity getViewer() {
        return viewer;
    }

    public void open() {
        if (!viewer.getOpenInventory().getTopInventory().equals(inventory)) {
            update();
            viewer.openInventory(inventory);
        }
    }

    public void update() {
        Inventory newInventory = host.getInventory(this);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, newInventory.getItem(i));
        }
    }

    public void next() {
        this.page++;
        update();
    }

    public void previous() {
        this.page--;
        update();
    }

    public void reset() {
        this.page = 0;
        update();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Anemone getHost() {
        return host;
    }

    public void onClick(InventoryClickEvent event) {
        Index index = fromSlot(event.getRawSlot());
        if (index == null) return;

        host.onClick(index, this, event);
    }

    public void onDrag(InventoryDragEvent event) {
        ArrayList<Index> indices = new ArrayList<>();
        for (int rawSlot : event.getRawSlots()) {
            Index index = fromSlot(rawSlot);
            if (index != null) indices.add(index);
        }

        host.onDrag(indices, this, event);
    }

    public void onClose(InventoryCloseEvent event) {
        host.onClose(this);
    }

    public void onOpen(InventoryOpenEvent event) {
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
