package coolclk.escape.material.item;

import coolclk.escape.material.meta.CustomItemMeta;
import coolclk.escape.material.CustomItemMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BulletMaterial extends CustomItemMaterial<CustomItemMeta> {
    public BulletMaterial() {
        super("bullet", Material.YELLOW_CANDLE);
    }

    @Nonnull
    @Override
    public CustomItemMeta getItemMeta(ItemStack itemStack) {
        return new CustomItemMeta(itemStack);
    }

    @Nonnull
    @Override
    public CustomItemMeta getDefaultItemMeta() {
        CustomItemMeta meta = new CustomItemMeta();
        meta.setCustomModelData(12340001);
        meta.setDisplayName("子弹");
        meta.setMaxStackSize(99);
        return meta;
    }

    @Override
    public CustomItemMaterial.InteractResult interact(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        CustomItemMaterial.InteractResult result = new CustomItemMaterial.InteractResult();
        result.setUseItemInHand(Event.Result.DENY);
        return result;
    }
}
