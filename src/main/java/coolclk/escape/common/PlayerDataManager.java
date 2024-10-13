package coolclk.escape.common;

import com.google.gson.Gson;
import coolclk.escape.Escape;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class PlayerDataManager {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File getPlayerDataFolder() {
        Escape.instance().getDataFolder().mkdirs();
        File file = new File(Escape.instance().getDataFolder(), "playerdata");
        if ((file.exists() && file.isDirectory()) || file.mkdirs()) {
            return file;
        }
        return null;
    }

    private static File getPlayerDataFile(Player player) throws IOException {
        File dataFolder = getPlayerDataFolder();
        File data = new File(dataFolder, player.getUniqueId() + ".json");
        if ((!data.exists() || !data.isFile()) && data.createNewFile()) {
            String json = new Gson().toJson(new PlayerData());
            try (FileWriter writer = new FileWriter(data)) {
                writer.write(json);
                writer.flush();
            }
        }
        return data;
    }

    public static PlayerData getPlayerData(Player player) {
        try {
            return new Gson().fromJson(new FileReader(getPlayerDataFile(player)), PlayerData.class);
        } catch (IOException e) {
            throw new RuntimeException("failed to get data of " + player, e);
        }
    }

    public static void modifyPlayerData(Player player, Consumer<PlayerData> function) {
        PlayerData playerData = getPlayerData(player);
        if (playerData != null) {
            String ordinalJson = new Gson().toJson(playerData);
            function.accept(playerData);
            String newJson = new Gson().toJson(playerData);
            if (!Objects.equals(newJson, ordinalJson)) {
                try {
                    try (FileWriter writer = new FileWriter(getPlayerDataFile(player))) {
                        writer.write(newJson);
                        writer.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("when write data of player " + player.getName(), e);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Getter
    public static class PlayerData {
        @Getter
        public static class Attribute {
            @Setter private double Base;
            private final String Name;

            private Attribute(double base, String name) {
                this.Base = base;
                this.Name = name;
            }
        }

        @Getter
        public static class ItemStack {
            private final Map<String, ?> components = new HashMap<>();
            @Setter private int count;
            @Setter private byte Slot;
            private final String id;

            private ItemStack(String id) {
                this.id = id;
            }
        }

        private final List<Attribute> Attributes = List.of(
                new Attribute(100, "escape:generic.max_energy")
        );
        @Setter private int Gold = 0;
        private int Energy = 0;
        //private final List<ItemStack> EncryptionItems = List.of();

        public double getMaxEnergy() {
            Optional<Attribute> optional = this.getAttributes().stream().filter(attribute -> Objects.equals(attribute.getName(), "escape:generic.max_energy")).findAny();
            return optional.map(Attribute::getBase).orElse(0.0);
        }

        public void setMaxEnergy(int base) {
            this.getAttributes().stream().filter(attribute -> Objects.equals(attribute.getName(), "escape:generic.max_energy")).findAny().ifPresent(attribute -> {
                attribute.setBase(base);
            });
        }

        public void setEnergy(int energy) {
            if (energy > this.getMaxEnergy()) {
                energy = (int) this.getMaxEnergy();
            }
            this.Energy = energy;
        }
    }
}
