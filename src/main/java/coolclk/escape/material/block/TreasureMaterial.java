package coolclk.escape.material.block;

import coolclk.escape.Escape;
import coolclk.escape.common.TreasureManager;
import coolclk.escape.material.CustomBlockMaterial;
import coolclk.escape.material.meta.CustomBlockItemMeta;
import coolclk.escape.material.meta.CustomBlockMeta;
import coolclk.escape.material.meta.block.TreasureBlockMeta;
import coolclk.escape.material.meta.blockitem.TreasureBlockItemMeta;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Getter
public class TreasureMaterial extends CustomBlockMaterial<TreasureBlockMeta, TreasureBlockItemMeta> {
    private final ItemRarity rarity;

    public TreasureMaterial(ItemRarity rarity) {
        super("treasure_" + rarity.name().toLowerCase(), Material.CHEST);
        this.rarity = rarity;
    }

    @Nonnull
    @Override
    public TreasureBlockItemMeta getItemMeta(ItemStack itemStack) {
        return new TreasureBlockItemMeta(itemStack);
    }

    @Nonnull
    @Override
    public TreasureBlockItemMeta getDefaultItemMeta() {
        TreasureBlockItemMeta meta = new TreasureBlockItemMeta();
        meta.setTreasureRarity(this.getRarity());
        meta.setCountdown(100L);
        switch (meta.getTreasureRarity()) {
            case COMMON -> meta.setDisplayName("§2平凡箱子");
            case UNCOMMON -> meta.setDisplayName("§b非凡箱子");
            case RARE -> meta.setDisplayName("§e稀有箱子");
            case EPIC -> meta.setDisplayName("§d史诗箱子");
        }
        return meta;
    }

    @Nonnull
    @Override
    public TreasureBlockMeta getBlockMeta(Block block) {
        return new TreasureBlockMeta(block);
    }

    @Nonnull
    @Override
    public TreasureBlockMeta getBlockMeta(ItemStack itemStack) {
        return new TreasureBlockMeta(itemStack);
    }

    @Override
    public InteractResult beInteracted(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        if (clickedBlock != null && action == Action.RIGHT_CLICK_BLOCK && clickedBlock.getType() == this.currentMaterial()) {
            TreasureBlockMeta meta = getBlockMeta(clickedBlock);
            if (meta.hasTreasureRarity() && !meta.isOpened()) {
                final long countdown = meta.hasCountdown() ? meta.getCountdown() : 0;
                ArmorStand progress = (ArmorStand) player.getWorld().spawnEntity(clickedBlock.getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                progress.setInvisible(true);
                progress.setInvulnerable(true);
                progress.setMarker(true);
                progress.setGravity(false);
                meta.setOpened(true);
                this.applyBlockMeta(clickedBlock, meta);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (clickedBlock.getType() != currentMaterial()) {
                            progress.remove();
                            this.cancel();
                            return;
                        }
                        TreasureBlockMeta meta = getBlockMeta(clickedBlock);
                        meta.setCountdown(meta.getCountdown() - 1L);
                        progress.setCustomName(progress("§f■", "§a■", (double) (countdown - meta.getCountdown()) / countdown, 10));
                        progress.setCustomNameVisible(true);
                        if (meta.getCountdown() <= 0) {
                            meta.setOpened(true);
                            TreasureManager.lootTreasureTable(((Chest) clickedBlock.getState()).getBlockInventory(), meta.getTreasureRarity());
                            progress.remove();
                            this.cancel();
                        }
                        applyBlockMeta(clickedBlock, meta);
                    }
                }.runTaskTimer(Escape.instance(), 0, 1L);
            }
            if (meta.getCountdown() > 0) {
                InteractResult result = new InteractResult();
                result.setCancelled(true);
                return result;
            }
        }
        return super.beInteracted(player, action, itemStack, clickedBlock, clickedFace, hand, clickedPosition);
    }

    @Override
    public void applyItemMeta(ItemStack itemStack, TreasureBlockItemMeta customItemMeta) {
        super.applyItemMeta(itemStack, customItemMeta);
        NBT.modify(itemStack, nbt -> {
            if (customItemMeta.hasTreasureRarity()) {
                nbt.setInteger(CustomBlockItemMeta.getKey("Rarity"), customItemMeta.getTreasureRarity().ordinal());
            } else if (nbt.hasTag(CustomBlockMeta.getKey("Rarity"))) {
                nbt.removeKey(CustomBlockMeta.getKey("Rarity"));
            }
            if (customItemMeta.hasCountdown()) {
                nbt.setLong(CustomBlockItemMeta.getKey("Countdown"), customItemMeta.getCountdown());
            } else if (nbt.hasTag(CustomBlockMeta.getKey("Countdown"))) {
                nbt.removeKey(CustomBlockMeta.getKey("Countdown"));
            }
        });
    }

    @Override
    public void applyBlockMeta(Block block, TreasureBlockMeta customBlockMeta) {
        super.applyBlockMeta(block, customBlockMeta);
        NBT.modify(block.getState(), root -> {
            ReadWriteNBT components = root.getCompound("components");
            if (components != null) {
                ReadWriteNBT customData = components.getCompound("minecraft:custom_data");
                if (customData != null) {
                    customData.setBoolean(CustomBlockMeta.getKey("Opened"), customBlockMeta.isOpened());
                    if (customBlockMeta.hasTreasureRarity()) {
                        customData.setInteger(CustomBlockMeta.getKey("Rarity"), customBlockMeta.getTreasureRarity().ordinal());
                    } else if (customData.hasTag(CustomBlockMeta.getKey("Rarity"))) {
                        customData.removeKey(CustomBlockMeta.getKey("Rarity"));
                    }
                    if (customBlockMeta.hasCountdown()) {
                        customData.setLong(CustomBlockMeta.getKey("Countdown"), customBlockMeta.getCountdown());
                    } else if (customData.hasTag(CustomBlockMeta.getKey("Countdown"))) {
                        customData.removeKey(CustomBlockMeta.getKey("Countdown"));
                    }
                }
            }
        });
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
}
