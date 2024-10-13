package coolclk.escape.common;

import coolclk.escape.Escape;
import coolclk.escape.material.block.TreasureMaterial;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class GameManager {
    public static List<String> gameList() {
        List<String> games = new ArrayList<>();
        for (File gameFolder : getGameDataFolders()) {
            if (!gameFolder.getName().contains(" ")) {
                games.add(gameFolder.getName());
            }
        }
        return List.copyOf(games);
    }

    public static boolean hasGame(String game) {
        return gameList().stream().anyMatch($game -> Objects.equals($game, game));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    public static List<TreasureConfiguration> gameTreasures(String game) {
        File gameFolder = getGameDataFolder(game);
        File treasuresFile = new File(gameFolder, "treasures.yml");
        if (!treasuresFile.exists() || !treasuresFile.isFile()) {
            try {
                treasuresFile.createNewFile();
            } catch (IOException ignored) {  }
        }
        List<TreasureConfiguration> treasures = new ArrayList<>();
        try {
            Map<String, Object> yaml = new Yaml().load(new FileInputStream(treasuresFile));
            if (yaml.containsKey("treasures") && yaml.get("treasures") instanceof List<?>) {
                for (Object treasureElement : (List<?>) yaml.get("treasures")) {
                    if (treasureElement instanceof Map<?, ?>) {
                        Map<String, Object> treasureObject = (Map<String, Object>) treasureElement;
                        if (treasureObject.containsKey("location") && treasureObject.containsKey("type")) {
                            Object locationElement = treasureObject.get("location");
                            Object typeElement = treasureObject.get("type");
                            if (locationElement instanceof Map<?, ?>) {
                                Map<String, Object> locationObject = (Map<String, Object>) locationElement;
                                String worldName = (String) locationObject.getOrDefault("world", null);
                                double x = (double) locationObject.getOrDefault("x", null);
                                double y = (double) locationObject.getOrDefault("y", null);
                                double z = (double) locationObject.getOrDefault("z", null);
                                World world;
                                if ((world = Bukkit.getWorld(worldName)) != null) {
                                    TreasureMaterial material;
                                    if ((material = MaterialManager.getBlock((String) typeElement)) != null) {
                                        TreasureConfiguration configuration = new TreasureConfiguration();
                                        configuration.setLocation(new Location(world, x, y, z));
                                        configuration.setType(material);
                                        treasures.add(configuration);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException ignored) {  }
        return List.copyOf(treasures);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void checkDataFolder() {
        Escape.instance().getDataFolder().mkdirs();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File getGamesDataFolder() {
        checkDataFolder();
        File dataFolder = new File(Escape.instance().getDataFolder(), "games");
        dataFolder.mkdirs();
        return dataFolder;
    }

    private static List<File> getGameDataFolders() {
        File[] files = getGamesDataFolder().listFiles();
        return files != null ? List.of(files) : Collections.emptyList();
    }

    private static File getGameDataFolder(String game) {
        return getGameDataFolders().stream().filter(folder -> folder.getName().equals(game)).findAny().orElse(null);
    }

    @Data
    public static class TreasureConfiguration {
        private Location location;
        private TreasureMaterial type;
    }
}
