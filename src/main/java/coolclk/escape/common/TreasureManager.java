package coolclk.escape.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import coolclk.escape.Escape;
import coolclk.escape.material.CustomItemMaterial;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class TreasureManager {
    private static final Map<String, TreasureManager.TreasureLootTable> inMemory = new HashMap<>();

    public static void reloadLootTables() {
        inMemory.clear();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void checkDataFolder() {
        Escape.instance().getDataFolder().mkdirs();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File getTreasuresDataFolder() {
        checkDataFolder();
        File dataFolder = new File(Escape.instance().getDataFolder(), "treasures");
        dataFolder.mkdirs();
        return dataFolder;
    }

    public static File getTreasureLootTableFile(String name) {
        File treasureLootTableFile = new File(getTreasuresDataFolder(), name + ".json");
        if (!treasureLootTableFile.exists() || !treasureLootTableFile.isFile()) {
            return null;
        }
        return treasureLootTableFile;
    }

    public static void loadTreasureLootTable(String name) {
        if (loadedTreasureLootTable(name)) {
            return;
        }
        File file = getTreasureLootTableFile(name);
        TreasureLootTable table;
        try {
            if (file != null) {
                table = new TreasureLootTable();
                JsonObject root = new Gson().fromJson(new FileReader(file), JsonObject.class);
                if (root.has("rolls") && root.has("entries")) {
                    TreasureLootTable.IntegerProvider rolls = TreasureLootTable.IntegerProvider.constant(0);
                    List<TreasureLootTable.Entry> entries = new ArrayList<>();

                    JsonElement rollsElement = root.get("rolls");
                    if (rollsElement.isJsonObject() && rollsElement.getAsJsonObject().has("type")) {
                        switch (rollsElement.getAsJsonObject().get("type").getAsString()) {
                            case "constant" -> rolls = TreasureLootTable.IntegerProvider.constant(rollsElement.getAsJsonObject().get("value").getAsInt());
                            case "random" -> rolls = TreasureLootTable.IntegerProvider.random(rollsElement.getAsJsonObject().get("origin").getAsInt(), rollsElement.getAsJsonObject().get("bound").getAsInt());
                        }
                    } else {
                        rolls = TreasureLootTable.IntegerProvider.constant(rollsElement.getAsInt());
                    }

                    JsonElement entriesElement = root.get("entries");
                    if (entriesElement.isJsonArray()) {
                        for (JsonElement entryElement : entriesElement.getAsJsonArray()) {
                            if (entryElement.isJsonObject() && entryElement.getAsJsonObject().has("name")) {
                                JsonObject entryObject = entryElement.getAsJsonObject();
                                TreasureLootTable.Entry entry = new TreasureLootTable.Entry();
                                entry.setName(entryObject.get("name").getAsString());
                                if (entryObject.has("weight")) {
                                    entry.setWeight(entryObject.get("weight").getAsInt());
                                }
                                if (entryObject.has("count")) {
                                    JsonElement countElement = entryObject.get("count");
                                    TreasureLootTable.IntegerProvider count = null;
                                    if (countElement.isJsonObject() && countElement.getAsJsonObject().has("type")) {
                                        switch (countElement.getAsJsonObject().get("type").getAsString()) {
                                            case "constant" -> count = TreasureLootTable.IntegerProvider.constant(countElement.getAsJsonObject().get("value").getAsInt());
                                            case "random" -> count = TreasureLootTable.IntegerProvider.random(countElement.getAsJsonObject().get("origin").getAsInt(), countElement.getAsJsonObject().get("bound").getAsInt());
                                        }
                                    } else {
                                        count = TreasureLootTable.IntegerProvider.constant(countElement.getAsInt());
                                    }
                                    if (count != null) {
                                        entry.setCount(count);
                                    }
                                }
                                entries.add(entry);
                            }
                        }
                    }

                    table.setRolls(rolls);
                    table.setCurrentEntries(entries);

                    if (root.has("parent")) {
                        final String parentName = root.get("parent").getAsString();
                        loadTreasureLootTable(parentName);
                        Bukkit.getConsoleSender().sendMessage("§7[§bEscape§7] §aLoaded treasure table " + parentName + " as the parent of " + name);
                        table.setParent(inMemory.getOrDefault(parentName, null));
                    }
                }
                inMemory.put(name, table);
            }
        } catch (FileNotFoundException ignored) {  }
    }

    private static boolean loadedTreasureLootTable(String name) {
        return inMemory.containsKey(name);
    }

    public static TreasureLootTable getTreasureLootTable(String name) {
        if (!loadedTreasureLootTable(name)) {
            loadTreasureLootTable(name);
        }
        return inMemory.get(name);
    }

    public static TreasureLootTable getTreasureLootTable(ItemRarity rarity) {
        return getTreasureLootTable(rarity.name().toLowerCase());
    }

    public static void lootTreasureTable(Inventory inventory, String name) {
        TreasureLootTable table = getTreasureLootTable(name);
        if (table != null) {
            inventory.clear();
            int rolls = table.getRolls().provide();
            if (!table.getEntries().isEmpty()) {
                AtomicInteger entriesWeights = new AtomicInteger();
                table.getEntries().forEach(entry -> entriesWeights.addAndGet(entry.getWeight()));
                for (int roll = 0; roll < rolls; roll++) {
                    int slot;
                    ItemStack targetSlot;
                    do {
                        slot = new Random().nextInt(inventory.getSize() - 1);
                    } while ((targetSlot = inventory.getItem(slot)) != null && (targetSlot.getType() != Material.AIR || targetSlot.getAmount() > 0));
                    int entryIndex = 0;
                    int weightIndex = (int) (Math.random() * entriesWeights.get());
                    for (int i = 0; i < weightIndex;) {
                        i += table.getEntries().get(entryIndex).getWeight();
                        entryIndex++;
                    }
                    if (entryIndex >= 0 && entryIndex < table.getEntries().size()) {
                        TreasureLootTable.Entry entry = table.getEntries().get(entryIndex);
                        if (entry.getName().startsWith("escape:")) {
                            CustomItemMaterial<?> material = MaterialManager.get(entry.getName());
                            if (material != null) {
                                inventory.setItem(slot, material.createItemStack(entry.getCount().provide()));
                            }
                        } else {
                            final int finalSlot = slot;
                            Arrays.stream(Material.values()).filter(material -> material.getKey().toString().equals(entry.getName()) || material.getKey().getKey().equals(entry.getName())).findAny().ifPresent(material -> inventory.setItem(finalSlot, new ItemStack(material, entry.getCount().provide())));
                        }
                    }
                }
            }
        }
    }

    public static void lootTreasureTable(Inventory inventory, ItemRarity rarity) {
        lootTreasureTable(inventory, rarity.name().toLowerCase());
    }

    @Getter
    @Setter
    public static class TreasureLootTable {
        private TreasureLootTable parent;
        private IntegerProvider rolls;
        private List<Entry> currentEntries;

        @Getter
        public static abstract class IntegerProvider {
            private final String type;

            public IntegerProvider(String type) {
                this.type = type;
            }

            public abstract int provide();

            public static ConstantIntegerProvider constant(int value) {
                return new ConstantIntegerProvider(value);
            }

            public static RandomIntegerProvider random(int origin, int bound) {
                return new RandomIntegerProvider(origin, bound);
            }
        }

        public static class ConstantIntegerProvider extends IntegerProvider {
            private final int value;

            public ConstantIntegerProvider(int value) {
                super("constant");
                this.value = value;
            }

            @Override
            public int provide() {
                return this.value;
            }
        }

        public static class RandomIntegerProvider extends IntegerProvider {
            private final int origin;
            private final int bound;

            public RandomIntegerProvider(int origin, int bound) {
                super("random");
                this.origin = origin;
                this.bound = bound;
            }

            @Override
            public int provide() {
                return new Random().nextInt(this.origin, this.bound);
            }
        }

        public List<Entry> getEntries() {
            List<Entry> entries = new ArrayList<>(this.currentEntries);
            if (this.getParent() != null) {
                entries.addAll(this.getParent().getEntries());
            }
            return List.copyOf(entries);
        }

        @Getter
        @Setter
        public static class Entry {
            private String name;
            private int weight = 1;
            private IntegerProvider count = IntegerProvider.constant(1);
        }
    }
}
