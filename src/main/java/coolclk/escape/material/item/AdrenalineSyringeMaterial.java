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

public class AdrenalineSyringeMaterial extends ConsumableMaterial<ConsumableItemMeta> {
    public AdrenalineSyringeMaterial() {
        super("adrenaline_syringe", Material.ARROW);
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
        meta.setDisplayName("肾上腺素");
        meta.setDefaultCountdown(160L);
        meta.setMaxStackSize(4);
        return meta;
    }

    @Override
    protected void consume(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        super.consume(player, action, itemStack, clickedBlock, clickedFace, hand, clickedPosition);
        PlayerDataManager.modifyPlayerData(player, playerData -> playerData.setEnergy(playerData.getEnergy() + 100));
    }
}
