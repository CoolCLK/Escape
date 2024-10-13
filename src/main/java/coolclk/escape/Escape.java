package coolclk.escape;

import coolclk.escape.common.MaterialManager;
import coolclk.escape.command.EscapeCommand;
import coolclk.escape.event.EventListener;
import coolclk.escape.material.block.TreasureMaterial;
import coolclk.escape.material.item.*;
import coolclk.escape.material.meta.CustomItemMeta;
import coolclk.escape.common.PlayerDataManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

@SuppressWarnings("unused")
public final class Escape extends JavaPlugin {
    @Getter
    private final String id = "escape";
    @Getter
    private final boolean debug = false;
    private final EscapeCommand command = new EscapeCommand("escape");
    private final BukkitRunnable ticking = new BukkitRunnable() {
        private int timer = 0;

        @Override
        public void run() {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                ItemStack mainHand = player.getInventory().getItemInMainHand(), offHand = player.getInventory().getItemInOffHand();
                if (mainHand.getType() != Material.AIR) {
                    String id = CustomItemMeta.getId(mainHand);
                    MaterialManager.itemMaterials().stream().filter(material -> Objects.equals(material.id(), id)).findAny().ifPresent(material -> material.tick(player, mainHand, EquipmentSlot.HAND));
                }
                if (offHand.getType() != Material.AIR) {
                    String id = CustomItemMeta.getId(offHand);
                    MaterialManager.itemMaterials().stream().filter(material -> Objects.equals(material.id(), id)).findAny().ifPresent(material -> material.tick(player, offHand, EquipmentSlot.OFF_HAND));
                }

                PlayerDataManager.modifyPlayerData(player, playerData -> {
                    if (playerData.getEnergy() > 0) {
                        if (timer % 60 == 0) {
                            playerData.setEnergy(playerData.getEnergy() - 1);
                        }
                        int level = 0;
                        if (playerData.getEnergy() >= 90) {
                            level = 3;
                        } else if (playerData.getEnergy() >= 70) {
                            level = 2;
                        } else if (playerData.getEnergy() >= 40) {
                            level = 1;
                        }
                        if (timer % 160 == 0) {
                            AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (maxHealth != null) {
                                player.setHealth(Math.min(player.getHealth() + ((level + 1) * 0.2), maxHealth.getValue()));
                            }
                            if (level >= 2) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1, level - 2, false, false, false));
                            }
                        }
                    }
                    player.setExp((float) (playerData.getEnergy() / playerData.getMaxEnergy()));
                });
            });
            timer++;
        }
    };

    public static Escape instance() {
        return (Escape) JavaPlugin.getProvidingPlugin(Escape.class);
    }

    @Override
    public void onLoad() {
        MaterialManager.register(new GunMaterial());
        MaterialManager.register(new BulletMaterial());
        MaterialManager.register(new BandageMaterial());
        MaterialManager.register(new EnergyDrinkMaterial());
        MaterialManager.register(new PainkillerMaterial());
        MaterialManager.register(new AdrenalineSyringeMaterial());
        MaterialManager.register(new FirstAidKitMaterial());
        MaterialManager.register(new MedKitMaterial());
        MaterialManager.register(new TreasureMaterial(ItemRarity.COMMON));
        MaterialManager.register(new TreasureMaterial(ItemRarity.UNCOMMON));
        MaterialManager.register(new TreasureMaterial(ItemRarity.RARE));
        MaterialManager.register(new TreasureMaterial(ItemRarity.EPIC));
    }

    @Override
    public void onEnable() {
        this.ticking.runTaskTimer(this, 0, 1L);
        this.command.register();
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        this.ticking.cancel();
        this.command.unregister();
    }

    public void sendDebugMessage(String message) {
        if (this.isDebug()) {
            Bukkit.broadcastMessage("§7[§bEscape§7] §7[§4Debug§7] §a" + message);
        }
    }

    public static void debugMessage(String message) {
        instance().sendDebugMessage(message);
    }
}
