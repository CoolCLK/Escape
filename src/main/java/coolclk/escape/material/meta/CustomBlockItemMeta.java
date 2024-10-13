package coolclk.escape.material.meta;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CustomBlockItemMeta extends CustomItemMeta {
    public CustomBlockItemMeta() {

    }

    public CustomBlockItemMeta(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
            this.apply(itemStack);
        }
    }
}
