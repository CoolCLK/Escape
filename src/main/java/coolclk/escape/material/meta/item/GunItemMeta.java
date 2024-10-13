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
public class GunItemMeta extends CustomItemMeta {
    private Integer bulletDamage = 4;
    private Integer bulletCount;
    private Integer maxBulletCount;
    private boolean reloadingBullet;

    public GunItemMeta() {

    }

    public GunItemMeta(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
            this.apply(itemStack);
        }
    }

    protected void apply(ItemStack itemStack) {
        super.apply(itemStack);
        ReadableNBT nbt = NBT.readNbt(itemStack);
        this.setMaxBulletCount(nbt.hasTag(getKey("MaxBulletCount"), NBTType.NBTTagInt) ? nbt.getInteger(getKey("MaxBulletCount")) : null);
        this.setBulletCount(nbt.hasTag(getKey("BulletCount"), NBTType.NBTTagInt) ? nbt.getInteger(getKey("BulletCount")) : null);
        this.setReloadingBullet(nbt.hasTag(getKey("ReloadingBullet")) ? nbt.getBoolean(getKey("ReloadingBullet")) : false);
    }

    public boolean hasMaxBulletCount() {
        return this.getMaxBulletCount() != null;
    }

    public boolean hasBulletCount() {
        return this.getBulletCount() != null;
    }
}
