package coolclk.escape.material;

import coolclk.escape.material.meta.CustomBlockItemMeta;
import coolclk.escape.material.meta.CustomBlockMeta;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public abstract class CustomBlockMaterial<B extends CustomBlockMeta, I extends CustomBlockItemMeta> extends CustomItemMaterial<I> {
    public CustomBlockMaterial(String name, Material material) {
        super(name, material);
        if (!material.isBlock()) {
            throw new IllegalArgumentException("unsupported material " + material.name());
        }
    }

    public CustomBlockMaterial(String[] id, Material material) {
        super(id, material);
        if (!material.isBlock()) {
            throw new IllegalArgumentException("unsupported material " + material.name());
        }
    }

    @Nonnull
    public abstract B getBlockMeta(Block block);

    @Nonnull
    public abstract B getBlockMeta(ItemStack itemStack);

    public BreakResult broken(@Nonnull Player player, @Nullable Block block, int expToDrop) {
        return new BreakResult();
    }

    public InteractResult beInteracted(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        return new InteractResult();
    }

    public void applyBlockMeta(Block block, B customBlockMeta) {
        NBT.modify(block.getState(), root -> {
            if (customBlockMeta.hasCustomName()) {
                root.setString("CustomName", customBlockMeta.getCustomName());
            } else if (root.hasTag("CustomName", NBTType.NBTTagString)) {
                root.removeKey("CustomName");
            }
        });
    }

    @Data
    public static class BreakResult {
        private boolean cancelled;
        private Boolean dropItems;
        private Integer expToDrop;

        public BreakResult() {
            this.cancelled = false;
            this.dropItems = null;
            this.expToDrop = null;
        }
    }
}
