package coolclk.escape.common;

import coolclk.escape.material.CustomBlockMaterial;
import coolclk.escape.material.CustomItemMaterial;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Registry<T extends IRegistryEntry> {
    public static final Registry<CustomItemMaterial<?>> ITEM = new Registry<>();
    public static final Registry<CustomBlockMaterial<?, ?>> BLOCK = new Registry<>();
    private final List<T> entries = new ArrayList<>();

    public Registry() {

    }

    public T register(T entry) {
        this.entries.add(entry);
        return entry;
    }

    public List<T> getEntries() {
        return List.copyOf(entries);
    }

    public static <T extends IRegistryEntry> T register(T entry, Registry<T> registry) {
        return registry.register(entry);
    }

    public static CustomItemMaterial<?> registerItem(CustomItemMaterial<?> entry) {
        return register(entry, ITEM);
    }

    public static CustomBlockMaterial<?, ?> registerBlock(CustomBlockMaterial<?, ?> entry) {
        registerItem(entry);
        return register(entry, BLOCK);
    }

    public static <T extends IRegistryEntry> List<T> getEntries(Registry<T> registry) {
        return registry.getEntries();
    }

    public static List<CustomItemMaterial<?>> itemEntries() {
        return getEntries(ITEM);
    }

    public static List<CustomBlockMaterial<?, ?>> blockEntries() {
        return getEntries(BLOCK);
    }
}
