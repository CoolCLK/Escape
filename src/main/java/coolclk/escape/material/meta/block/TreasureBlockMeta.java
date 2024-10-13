package coolclk.escape.material.meta.block;

import coolclk.escape.Escape;
import coolclk.escape.material.meta.CustomBlockMeta;
import coolclk.escape.material.meta.blockitem.TreasureBlockItemMeta;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@SuppressWarnings("unused")
public class TreasureBlockMeta extends CustomBlockMeta {
    private boolean opened;
    private Long countdown;
    private ItemRarity treasureRarity;

    public TreasureBlockMeta() {
        super();
    }

    public TreasureBlockMeta(ItemStack itemStack) {
        super(itemStack);
    }

    public TreasureBlockMeta(Block block) {
        super(block);
    }

    @Override
    protected void apply(Block block) {
        super.apply(block);
        NBT.get(block.getState(), root -> {
            ReadableNBT components = root.getCompound("components");
            if (components != null) {
                ReadableNBT customData = components.getCompound("minecraft:custom_data");
                if (customData != null) {
                    this.setOpened(customData.hasTag(getKey("Opened")) ? customData.getBoolean(getKey("Opened")) : false);
                    if (customData.hasTag(getKey("Rarity"))) {
                        int rarityIndex = customData.getInteger(getKey("Rarity"));
                        this.setTreasureRarity(rarityIndex >= 0 && rarityIndex < ItemRarity.values().length ? ItemRarity.values()[rarityIndex] : null);
                    }
                    this.setCountdown(customData.hasTag(getKey("Countdown")) ? customData.getLong(getKey("Countdown")) : null);
                }
            }
        });
    }

    @Override
    protected void apply(ItemStack itemStack) {
        super.apply(itemStack);
        TreasureBlockItemMeta meta = new TreasureBlockItemMeta(itemStack);
        this.setOpened(false);
        this.setCountdown(meta.hasCountdown() ? meta.getCountdown() : null);
        this.setTreasureRarity(meta.hasTreasureRarity() ? meta.getTreasureRarity() : null);
    }

    public boolean hasCountdown() {
        return this.getCountdown() != null;
    }

    public boolean hasTreasureRarity() {
        return this.getTreasureRarity() != null;
    }

    @Override
    public String toString() {
        return "TreasureBlockMeta{" +
                "opened=" + this.isOpened() +
                ", countdown=" + this.getCountdown() +
                ", treasureRarity=" + this.getTreasureRarity() +
                '}';
    }
}
