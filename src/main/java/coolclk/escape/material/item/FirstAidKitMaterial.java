package coolclk.escape.material.item;

import coolclk.escape.material.meta.item.ConsumableItemMeta;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FirstAidKitMaterial extends ConsumableMaterial<ConsumableItemMeta> {
    public FirstAidKitMaterial() {
        super("first_aid_kit", Material.MAP);
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
        meta.setDisplayName("急救包");
        meta.setDefaultCountdown(120L);
        meta.setMaxStackSize(8);
        return meta;
    }

    @Override
    public InteractResult interact(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        if (itemStack != null && (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)) {
            AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth == null || player.getHealth() < maxHealth.getValue() * 0.75) {
                return super.interact(player, action, itemStack, clickedBlock, clickedFace, hand, clickedPosition);
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c你的血量已超过 75%"));
            InteractResult result = new InteractResult();
            result.setCancelled(true);
            return result;
        }
        return new InteractResult();
    }

    @Override
    protected void consume(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        super.consume(player, action, itemStack, clickedBlock, clickedFace, hand, clickedPosition);
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            player.setHealth(maxHealth.getValue() * 0.75);
        }
    }
}
