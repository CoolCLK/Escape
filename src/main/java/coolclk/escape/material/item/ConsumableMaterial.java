package coolclk.escape.material.item;

import coolclk.escape.Escape;
import coolclk.escape.common.PlayerDataManager;
import coolclk.escape.material.CustomItemMaterial;
import coolclk.escape.material.meta.CustomItemMeta;
import coolclk.escape.material.meta.item.ConsumableItemMeta;
import de.tr7zw.nbtapi.NBT;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class ConsumableMaterial<T extends ConsumableItemMeta> extends CustomItemMaterial<T> {
    protected ConsumableMaterial(String name, Material material) {
        super(name, material);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InteractResult interact(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        if (itemStack != null) {
            ConsumableItemMeta meta = new ConsumableItemMeta(itemStack);
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                if (!meta.hasCountdown()) {
                    meta.setCountdown(meta.getDefaultCountdown());
                    this.applyItemMeta(itemStack, (T) meta);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ConsumableItemMeta meta = new ConsumableItemMeta(itemStack);
                            if (meta.hasCountdown()) {
                                if (!Objects.equals(CustomItemMeta.getId(player.getInventory().getItemInMainHand()), id())) {
                                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(progress("§8■", "§c■", (double) (meta.getDefaultCountdown() - meta.getCountdown()) / meta.getDefaultCountdown(), 10)));
                                    meta.removeCountdown();
                                    applyItemMeta(itemStack, (T) meta);
                                    PlayerDataManager.modifyPlayerData(player, playerData -> {
                                        PlayerDataManager.getPlayerData(player).setEnergy(PlayerDataManager.getPlayerData(player).getEnergy() + 40);
                                    });
                                    this.cancel();
                                    return;
                                }
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1, 2, false, false, false));
                                meta.setCountdown(meta.getCountdown() - 1);
                                applyItemMeta(itemStack, (T) meta);
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(progress("§8■", "§f■", (double) (meta.getDefaultCountdown() - meta.getCountdown()) / meta.getDefaultCountdown(), 10)));
                                if (meta.getCountdown() <= 0) {
                                    consume(player, action, itemStack, clickedBlock, clickedFace, hand, clickedPosition);
                                    meta.removeCountdown();
                                    applyItemMeta(itemStack, (T) meta);
                                } else {
                                    return;
                                }
                            }
                            this.cancel();
                        }
                    }.runTaskTimer(Escape.instance(), 0, 1L);
                    InteractResult result = new InteractResult();
                    result.setCancelled(true);
                    return result;
                }
            }
        }
        return super.interact(player, action, itemStack, clickedBlock, clickedFace, hand, clickedPosition);
    }

    @SuppressWarnings("unused")
    protected void consume(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        if (itemStack != null) {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private String progress(String background, String force, double percent, int length) {
        StringBuilder builder = new StringBuilder();
        int forceLength = (int) (length * percent);
        for (int i = 0; i < length; i++) {
            if (i < forceLength) {
                builder.append(force);
            } else {
                builder.append(background);
            }
        }
        return builder.toString();
    }

    @Override
    public void applyItemMeta(ItemStack itemStack, T customItemMeta) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0) {
            return;
        }
        super.applyItemMeta(itemStack, customItemMeta);
        NBT.modify(itemStack, nbt -> {
            nbt.setLong(CustomItemMeta.getKey("DefaultCountdown"), customItemMeta.getDefaultCountdown());
            if (customItemMeta.getCountdown() == null) {
                nbt.removeKey(CustomItemMeta.getKey("Countdown"));
            } else {
                nbt.setLong(CustomItemMeta.getKey("Countdown"), customItemMeta.getCountdown());
            }
        });
    }
}
