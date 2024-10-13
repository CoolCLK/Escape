package coolclk.escape.material.meta;

import coolclk.escape.Escape;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
@SuppressWarnings("unused")
public class CustomBlockMeta {
    private String customName;

    private static String prefix() {
        return Escape.instance().getId();
    }

    public static String getKey(String key) {
        return CustomItemMeta.getKey(key);
    }

    public static boolean hasId(Block block) {
        return getId(block) != null;
    }

    public static String getId(Block block) {
        AtomicReference<String> id = new AtomicReference<>(null);
        if (block != null && block.getType() != Material.AIR && block.getState() instanceof TileState) {
            NBT.get(block.getState(), root -> {
                ReadableNBT components = root.getCompound("components");
                if (components != null) {
                    ReadableNBT customData = components.getCompound("minecraft:custom_data");
                    if (customData != null) {
                        id.set(customData.hasTag(getKey("id")) ? customData.getString(getKey("id")) : null);
                    }
                }
            });
        }
        return id.get();
    }

    public CustomBlockMeta() {
        this((ItemStack) null);
    }

    public CustomBlockMeta(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
            this.apply(itemStack);
        }
    }

    public CustomBlockMeta(Block block) {
        if (block != null && block.getType() != Material.AIR) {
            this.apply(block);
        }
    }

    protected void apply(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            this.setCustomName(meta.hasDisplayName() ? meta.getDisplayName() : null);
        }
    }

    protected void apply(Block block) {
        NBT.get(block.getState(), root -> {
            this.setCustomName(root.hasTag("CustomName", NBTType.NBTTagString) ? root.getString("CustomName") : null);
        });
    }

    public boolean hasCustomName() {
        return this.getCustomName() != null;
    }
}
