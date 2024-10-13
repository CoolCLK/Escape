package coolclk.escape.common;

import coolclk.escape.material.CustomBlockMaterial;
import coolclk.escape.material.CustomItemMaterial;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class MaterialManager {
    public static List<? extends CustomItemMaterial<?>> materials() {
        return Registry.itemEntries();
    }

    public static List<? extends CustomItemMaterial<?>> itemMaterials() {
        return Registry.itemEntries();
    }

    public static List<? extends CustomBlockMaterial<?, ?>> blockMaterials() {
        return Registry.blockEntries();
    }

    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public static <T extends CustomBlockMaterial<?, ?>> T register(T material) {
        return (T) Registry.registerBlock(material);
    }

    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public static <T extends CustomItemMaterial<?>> T register(T material) {
        return (T) Registry.registerItem(material);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomItemMaterial<?>> T get(String id) {
        return (T) itemMaterials().stream().filter(material -> Objects.equals(id, material.id())).findAny().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomItemMaterial<?>> T get(Class<T> type) {
        return (T) itemMaterials().stream().filter(material -> material.getClass() == type).findAny().orElse(null);
    }

    public static <T extends CustomItemMaterial<?>> T getItem(String id) {
        return get(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomItemMaterial<?>> T getItem(Class<T> type) {
        return (T) get(type);
    }

    public static <T extends CustomBlockMaterial<?, ?>> T getBlock(String id) {
        return get(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomItemMaterial<?>> T getBlock(Class<T> type) {
        return (T) get(type);
    }
}
