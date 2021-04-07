package io.github.bluelhf.anemone;

import io.github.bluelhf.anemone.gui.Anemone;
import io.github.bluelhf.anemone.gui.ViewContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Anemones is a handler singleton for Anemone subclasses. To use Anemones,
 * it must first be initialised with {@link Anemones#init(Plugin)}.
 * */
public class Anemones implements Listener {
    private static Anemones instance;
    private final HashMap<Class<? extends Anemone>, Anemone> anemoneRegistry = new HashMap<>();
    private final HashMap<HumanEntity, ViewContext> entityContexts = new HashMap<>();
    private final ArrayDeque<Plugin> hosts = new ArrayDeque<>();

    private Anemones(Plugin host) {
        hosts.add(host);
    }

    /**
     * Returns the initialised Anemones instance.
     * @return The initialised Anemones instance
     * */
    public static Anemones getInstance() {
        return instance;
    }

    /**
     * Initialises Anemones using the given plugin.
     * If Anemones is already initialised, the plugin will be added to the hosts deque of Anemones.
     * Anemones will only disable when the hosts queue is empty.
     * @param host The plugin to initialise Anemones with
     * */
    public static void init(Plugin host) {
        if (instance == null) {
            instance = new Anemones(host);
            Bukkit.getPluginManager().registerEvents(instance, host);
        } else {
            instance.hosts.add(host);
        }
    }

    /**
     * Registers a new subclass of Anemone. Useful if your Anemone subclass takes constructor parameters.
     * @param anemone The subclass of Anemone to register
     * @throws IllegalStateException If Anemones isn't initialised
     *
     * @see Anemones#register(Class)
     * */
    public static void register(Anemone anemone) {
        checkInit();
        instance._register(anemone);
    }

    /**
     * Registers a new subclass of Anemone using its default constructor.
     * @param anemoneClass The class to register
     * @throws IllegalStateException If Anemones isn't initialised
     * @throws IllegalArgumentException If the class does not have a default constructor.
     *
     * @see Anemones#register(Anemone)
     * */
    public static void register(Class<? extends Anemone> anemoneClass) {
        checkInit();
        instance._register(anemoneClass);
    }

    /**
     * Unregisters a subclass of Anemone. Does nothing if the class isn't registered.
     * @param anemoneClass The class to unregister
     * @throws IllegalStateException If Anemones isn't initialised
     * */
    public static void unregister(Class<? extends Anemone> anemoneClass) {
        checkInit();
        instance._unregister(anemoneClass);
    }

    /**
     * Opens a registered subclass of Anemone to the given {@link HumanEntity}.
     * @param entity The entity to open the Anemone to
     * @param anemoneClass The class of the Anemone to open
     * @throws IllegalStateException If Anemones isn't initialised
     * @return The resulting {@link ViewContext}
     * */
    public static ViewContext open(HumanEntity entity, Class<? extends Anemone> anemoneClass) {
        checkInit();
        return instance._open(entity, anemoneClass);
    }

    private static void checkInit() {
        if (instance == null) throw new IllegalStateException("Must call Anemones.init() first.");
    }

    private void _register(Anemone anemone) {
        anemoneRegistry.put(anemone.getClass(), anemone);
    }

    private void _register(Class<? extends Anemone> anemoneClass) {
        try {
            Constructor<? extends Anemone> constructor = anemoneClass.getConstructor();
            constructor.setAccessible(true);
            _register(constructor.newInstance());
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalArgumentException(
                    "Could not register because class " + anemoneClass.getSimpleName() + " does not have a default constructor. " +
                            "Please consider using Anemones.register(Anemone) instead."
            );
        }
    }

    private void _unregister(Class<? extends Anemone> anemoneClass) {
        anemoneRegistry.remove(anemoneClass);
    }

    private ViewContext _open(HumanEntity entity, Class<? extends Anemone> anemoneClass) {
        Anemone anemone = anemoneRegistry.get(anemoneClass);
        if (anemone == null)
            throw new IllegalArgumentException("Anemone subclass " + anemoneClass.getSimpleName() + " must be registered before use.");
        ViewContext context = new ViewContext(entity, anemone);
        entityContexts.put(entity, context);
        context.open();
        return context;
    }

    @EventHandler
    private void onDisable(PluginDisableEvent event) {
        hosts.remove(event.getPlugin());
        if (hosts.size() == 0) {
            close();
        }
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        if (!entityContexts.containsKey(event.getWhoClicked())) return;
        ViewContext context = entityContexts.get(event.getWhoClicked());
        context.onClick(event);
    }

    @EventHandler
    private void onDrag(InventoryDragEvent event) {
        if (!entityContexts.containsKey(event.getWhoClicked())) return;
        ViewContext context = entityContexts.get(event.getWhoClicked());
        context.onDrag(event);
    }

    @EventHandler
    private void onClose(InventoryCloseEvent event) {
        if (!entityContexts.containsKey(event.getPlayer())) return;
        ViewContext context = entityContexts.get(event.getPlayer());
        context.onClose();
        entityContexts.remove(context.getViewer());
    }

    @EventHandler
    private void onOpen(InventoryOpenEvent event) {
        if (!entityContexts.containsKey(event.getPlayer())) return;
        ViewContext context = entityContexts.get(event.getPlayer());
        context.onOpen();
    }

    private void close() {
        HandlerList.unregisterAll(this);
        hosts.clear();
        instance = null;
    }
}
