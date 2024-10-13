package coolclk.escape.material;

import coolclk.escape.Escape;
import coolclk.escape.common.IRegistryEntry;
import coolclk.escape.material.meta.CustomItemMeta;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("unused")
public abstract class CustomItemMaterial<T extends CustomItemMeta> implements IRegistryEntry {
    private static CustomItemMeta processCreationMeta(CustomItemMeta meta) {
        return meta;
    }

    private final Material material;
    private final String id;

    public CustomItemMaterial(String name, Material material) {
        this(new String[]{Escape.instance().getId(), name}, material);
    }

    public CustomItemMaterial(String[] id, Material material) {
        this.id = id[0] + ":" + id[1];
        this.material = material;
    }

    public final String id() {
        return this.id;
    }

    public final Material currentMaterial() {
        return this.material;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    @Nonnull
    public final ItemStack createItemStack(int count) {
        ItemStack itemStack = new ItemStack(this.currentMaterial(), count, (short) 0);
        CustomItemMeta.setId(itemStack, this.id());
        T meta = this.getDefaultItemMeta();
        this.applyItemMeta(itemStack, (T) processCreationMeta(meta));
        return itemStack;
    }

    @Nonnull
    public abstract T getItemMeta(ItemStack itemStack);

    @Nonnull
    public abstract T getDefaultItemMeta();

    public boolean containsMaterial(Inventory inventory) {
        return Arrays.stream(inventory.getContents()).anyMatch(itemStack -> Objects.equals(CustomItemMeta.getId(itemStack), this.id()));
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public void applyItemMeta(ItemStack itemStack, T customItemMeta) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0) {
            return;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (customItemMeta.hasDisplayName()) {
                meta.setDisplayName("§r" + customItemMeta.getDisplayName());
            }
            if (customItemMeta.hasItemName()) {
                meta.setItemName(customItemMeta.getItemName());
            }
            if (customItemMeta.hasLore()) {
                meta.setLore(customItemMeta.getLore());
            }
            if (customItemMeta.hasCustomModelData()) {
                meta.setCustomModelData(customItemMeta.getCustomModelData());
            }
            if (customItemMeta.hasEnchantments()) {
                meta.removeEnchantments();
                customItemMeta.getEnchantments().forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
                meta.removeItemFlags(meta.getItemFlags().toArray(new ItemFlag[0]));
            }
            if (customItemMeta.hasItemFlags()) {
                meta.addItemFlags(customItemMeta.getItemFlags().toArray(new ItemFlag[0]));
            }
            if (customItemMeta.hasHideTooltip()) {
                meta.setHideTooltip(customItemMeta.getHideTooltip());
            }
            if (customItemMeta.hasUnbreakable()) {
                meta.setUnbreakable(customItemMeta.getUnbreakable());
            }
            if (customItemMeta.hasEnchantmentGlintOverride()) {
                meta.setUnbreakable(customItemMeta.getEnchantmentGlintOverride());
            }
            if (customItemMeta.hasMaxStackSize()) {
                meta.setMaxStackSize(customItemMeta.getMaxStackSize());
            }
            if (customItemMeta.hasRarity()) {
                meta.setRarity(customItemMeta.getRarity());
            }
            if (customItemMeta.hasFood()) {
                meta.setFood(customItemMeta.getFood());
            }
            if (customItemMeta.hasTool()) {
                meta.setTool(customItemMeta.getTool());
            }
            if (customItemMeta.hasDamage()) {
                ((Damageable) meta).setDamage(customItemMeta.getDamage());
            }
            if (customItemMeta.hasMaxDamage()) {
                ((Damageable) meta).setMaxDamage(customItemMeta.getMaxDamage());
            }
            if (customItemMeta.hasBasePotionType()) {
                ((PotionMeta) meta).setBasePotionType(customItemMeta.getBasePotionType());
            }
            if (customItemMeta.hasPotionColor()) {
                ((PotionMeta) meta).setColor(customItemMeta.getPotionColor());
            }
            if (customItemMeta.hasCustomEffects()) {
                ((PotionMeta) meta).clearCustomEffects();
                customItemMeta.getCustomEffects().forEach((effect) -> ((PotionMeta) meta).addCustomEffect(effect, true));
            }
        }
        itemStack.setItemMeta(meta);
    }

    @Nonnull
    public final ItemStack createItemStack() {
        return this.createItemStack(1);
    }

    public InteractResult interact(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        return new InteractResult();
    }

    public void tick(@Nonnull Player player, @Nonnull ItemStack itemStack, @Nullable EquipmentSlot hand) {
    }

    @Data
    public static class InteractResult {
        private boolean cancelled;
        private Event.Result useItemInHand;
        private Event.Result useInteractedBlock;

        public InteractResult() {
            this.cancelled = false;
            this.useItemInHand = Event.Result.DEFAULT;
            this.useInteractedBlock = Event.Result.DEFAULT;
        }
    }
}
