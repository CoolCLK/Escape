package coolclk.escape.material.meta.item;

import coolclk.escape.material.meta.CustomItemMeta;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@SuppressWarnings("unused")
public class ConsumableItemMeta extends CustomItemMeta {
    private Long defaultCountdown;
    private Long countdown;

    public ConsumableItemMeta() {

    }

    public ConsumableItemMeta(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
            this.apply(itemStack);
        }
    }

    protected void apply(ItemStack itemStack) {
        super.apply(itemStack);
        ReadableNBT nbt = NBT.readNbt(itemStack);
        this.setDefaultCountdown(nbt.hasTag(getKey("DefaultCountdown"), NBTType.NBTTagLong) ? nbt.getLong(getKey("DefaultCountdown")) : null);
        this.setCountdown(nbt.hasTag(getKey("Countdown"), NBTType.NBTTagLong) ? nbt.getLong(getKey("Countdown")) : null);
    }

    public void removeCountdown() {
        this.setCountdown(null);
    }

    public boolean hasCountdown() {
        return this.getCountdown() != null;
    }
}
