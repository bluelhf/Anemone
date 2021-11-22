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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Anemones is a handler singleton for Anemone subclasses. To use Anemones,
 * it must first be initialised with {@link Anemones#init(Plugin)}.
 * */
@SuppressWarnings("unused") // API
public class Anemones implements Listener {
    private static @Nullable Anemones instance;
    private final HashMap<Class<? extends Anemone>, Anemone> anemoneRegistry = new HashMap<>();
    private final HashMap<HumanEntity, ViewContext> entityContexts = new HashMap<>();
    private final ArrayDeque<Plugin> hosts = new ArrayDeque<>();

    private Anemones(@NotNull Plugin host) {
        hosts.add(host);
    }

    /**
     * Returns the initialised Anemones instance.
     * @return The initialised Anemones instance
     * */
    public static @Nullable Anemones getInstance() {
        return instance;
    }

    /**
     * Initialises Anemones using the given plugin.
     * If Anemones is already initialised, the plugin will be added to the hosts deque of Anemones.
     * Anemones will only disable when the hosts queue is empty.
     * @param host The plugin to initialise Anemones with
     * */
    public static void init(@NotNull Plugin host) {
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
    public static void register(@NotNull Anemone anemone) {
        checkInit();
        //noinspection ConstantConditions because we just checked you fucking idiot
        instance.register0(anemone);
    }

    /**
     * Registers a new subclass of Anemone using its default constructor.
     * @param anemoneClass The class to register
     * @throws IllegalStateException If Anemones isn't initialised
     * @throws IllegalArgumentException If the class does not have a default constructor.
     *
     * @see Anemones#register(Anemone)
     * */
    public static void register(@NotNull Class<? extends Anemone> anemoneClass) {
        checkInit();
        //noinspection ConstantConditions because we just checked
        instance.register0(anemoneClass);
    }

    /**
     * Unregisters a subclass of Anemone. Does nothing if the class isn't registered.
     * @param anemoneClass The class to unregister
     * @throws IllegalStateException If Anemones isn't initialised
     * */
    public static void unregister(Class<? extends Anemone> anemoneClass) {
        checkInit();
        //noinspection ConstantConditions because we just checked
        instance.unregister0(anemoneClass);
    }

    /**
     * Opens a registered subclass of Anemone to the given {@link HumanEntity}.
     * @param entity The entity to open the Anemone to
     * @param anemoneClass The class of the Anemone to open
     * @throws IllegalStateException If Anemones isn't initialised
     * @return The resulting {@link ViewContext}
     * */
    public static @NotNull ViewContext open(HumanEntity entity, @NotNull Class<? extends Anemone> anemoneClass) {
        checkInit();
        //noinspection ConstantConditions because we just checked
        return instance.open0(entity, anemoneClass);
    }

    /**
     * Throws an {@link IllegalStateException} if Anemones isn't initialised
     * @throws IllegalStateException When Anemones isn't initialised
     * */
    private static void checkInit() {
        if (instance == null) throw new IllegalStateException("Must call Anemones.init() first.");
    }

    /**
     * @see Anemones#register(Anemone)
     * @hidden Internal use only.
     * */
    private void register0(@NotNull Anemone anemone) {
        anemoneRegistry.put(anemone.getClass(), anemone);
        anemone.onRegister();
    }

    /**
     * @see Anemones#register(Class)
     * @hidden Internal use only.
     * */
    private void register0(@NotNull Class<? extends Anemone> anemoneClass) {
        try {
            Constructor<? extends Anemone> constructor = anemoneClass.getConstructor();
            constructor.setAccessible(true);
            register0(constructor.newInstance());
        } catch (@NotNull NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalArgumentException(
                    "Could not register because class " + anemoneClass.getSimpleName() + " does not have a default constructor. " +
                            "Please consider using Anemones.register(Anemone) instead."
            );
        }
    }

    /**
     * @see Anemones#unregister(Class)
     * @hidden Internal use only.
     * */
    private void unregister0(Class<? extends Anemone> anemoneClass) {
        Anemone anemone;
        if ((anemone = anemoneRegistry.remove(anemoneClass)) != null) {
            anemone.onUnregister();
        }
    }

    /**
     * @see Anemones#open(HumanEntity, Class)
     * @hidden Internal use only.
     * */
    private @NotNull ViewContext open0(HumanEntity entity, @NotNull Class<? extends Anemone> anemoneClass) {
        Anemone anemone = anemoneRegistry.get(anemoneClass);
        if (anemone == null)
            throw new IllegalArgumentException("Anemone subclass " + anemoneClass.getSimpleName() + " must be registered before use.");
        ViewContext context = new ViewContext(entity, anemone);
        entityContexts.put(entity, context);
        context.open();
        return context;
    }

    @EventHandler
    private void onDisable(@NotNull PluginDisableEvent event) {
        hosts.remove(event.getPlugin());
        if (hosts.size() == 0) {
            close();
        }
    }

    @EventHandler
    private void onClick(@NotNull InventoryClickEvent event) {
        if (!entityContexts.containsKey(event.getWhoClicked())) return;
        ViewContext context = entityContexts.get(event.getWhoClicked());
        context.onClick(event);
    }

    @EventHandler
    private void onDrag(@NotNull InventoryDragEvent event) {
        if (!entityContexts.containsKey(event.getWhoClicked())) return;
        ViewContext context = entityContexts.get(event.getWhoClicked());
        context.onDrag(event);
    }

    @EventHandler
    private void onClose(@NotNull InventoryCloseEvent event) {
        if (!entityContexts.containsKey(event.getPlayer())) return;
        ViewContext context = entityContexts.get(event.getPlayer());
        context.onClose();
        entityContexts.remove(context.getViewer());
    }

    @EventHandler
    private void onOpen(@NotNull InventoryOpenEvent event) {
        if (!entityContexts.containsKey(event.getPlayer())) return;
        ViewContext context = entityContexts.get(event.getPlayer());
        context.onOpen();
    }

    /**
     * Closes Anemones. Executed automatically.
     * @hidden Internal use only.
     * */
    private void close() {
        HandlerList.unregisterAll(this);
        hosts.clear();
        instance = null;
    }
}
