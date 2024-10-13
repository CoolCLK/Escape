package coolclk.escape.material.item;

import coolclk.escape.common.PlayerDataManager;
import coolclk.escape.material.meta.item.ConsumableItemMeta;
import org.bukkit.Color;
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

public class EnergyDrinkMaterial extends ConsumableMaterial<ConsumableItemMeta> {
    public EnergyDrinkMaterial() {
        super("energy_drink", Material.POTION);
    }

    @Nonnull
    @Override
    public ConsumableItemMeta getItemMeta(ItemStack itemStack) {
        return new ConsumableItemMeta(itemStack);
    }

    @Nonnull
    @Override
    public ConsumableItemMeta getDefaultItemMeta() {
        ConsumableItemMeta meta = new ConsumableItemMeta();
        meta.setCustomModelData(12340001);
        meta.setPotionColor(Color.WHITE);
        meta.setDisplayName("能量饮料");
        meta.setDefaultCountdown(80L);
        meta.setMaxStackSize(8);
        return meta;
    }

    @Override
    protected void consume(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        super.consume(player, action, itemStack, clickedBlock, clickedFace, hand, clickedPosition);
        PlayerDataManager.modifyPlayerData(player, playerData -> playerData.setEnergy(playerData.getEnergy() + 40));
    }
}
