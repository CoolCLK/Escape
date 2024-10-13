package coolclk.escape.material.meta.blockitem;

import coolclk.escape.Escape;
import coolclk.escape.material.meta.CustomBlockItemMeta;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@SuppressWarnings("unused")
public class TreasureBlockItemMeta extends CustomBlockItemMeta {
    private Long countdown;
    private ItemRarity treasureRarity;

    public TreasureBlockItemMeta() {
        super();
    }

    public TreasureBlockItemMeta(ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    protected void apply(ItemStack itemStack) {
        super.apply(itemStack);
        ReadableNBT nbt = NBT.readNbt(itemStack);
        this.setCountdown(nbt.hasTag(getKey("Countdown"), NBTType.NBTTagLong) ? nbt.getLong(getKey("Countdown")) : null);
        if (nbt.hasTag(getKey("Rarity"), NBTType.NBTTagInt)) {
            int rarityIndex = nbt.getInteger(getKey("Rarity"));
            this.setTreasureRarity(rarityIndex >= 0 && rarityIndex < ItemRarity.values().length ? ItemRarity.values()[rarityIndex] : null);
        }
    }

    public boolean hasCountdown() {
        return this.getCountdown() != null;
    }

    public boolean hasTreasureRarity() {
        return this.getTreasureRarity() != null;
    }

    @Override
    public String toString() {
        return "TreasureBlockItemMeta{" +
                "countdown=" + countdown +
                ", treasureRarity=" + treasureRarity +
                '}';
    }
}
