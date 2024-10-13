package coolclk.escape.material.meta;

import coolclk.escape.Escape;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@SuppressWarnings("unused")
public class CustomItemMeta {
    private static String prefix() {
        return Escape.instance().getId();
    }

    public static String getKey(String key) {
        return prefix() + ":" + key;
    }

    public static boolean hasId(ItemStack itemStack) {
        return getId(itemStack) != null;
    }

    public static String getId(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
            ReadableNBT nbt = NBT.readNbt(itemStack);
            return nbt.hasTag(getKey("id")) ? nbt.getString(getKey("id")) : null;
        }
        return null;
    }

    public static void setId(ItemStack itemStack, String id) {
        if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
            NBT.modify(itemStack, nbt -> {
                nbt.setString(getKey("id"), id);
            });
        }
    }

    private String displayName;
    private String itemName;
    private List<String> lore;
    private Integer customModelData;
    private Map<Enchantment, Integer> enchantments;
    private List<ItemFlag> itemFlags;
    private Boolean hideTooltip;
    private Boolean unbreakable;
    private Boolean enchantmentGlintOverride;
    private Boolean fireResistant;
    private Integer maxStackSize;
    private ItemRarity rarity;
    @SuppressWarnings("UnstableApiUsage") private FoodComponent food;
    @SuppressWarnings("UnstableApiUsage") private ToolComponent tool;
    @SuppressWarnings("deprecation") private CustomItemTagContainer customTagContainer;

    private Integer damage;
    private Integer maxDamage;

    private PotionType basePotionType;
    private List<PotionEffect> customEffects;
    private Color potionColor;

    public CustomItemMeta() {

    }

    public CustomItemMeta(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
            this.apply(itemStack);
        }
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    protected void apply(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            this.setDisplayName(meta.hasDisplayName() ? meta.getDisplayName() : null);
            this.setItemName(meta.hasItemName() ? meta.getItemName() : null);
            this.setLore(meta.hasLore() ? meta.getLore() : null);
            this.setCustomModelData(meta.hasCustomModelData() ? meta.getCustomModelData() : null);
            this.setEnchantments(meta.hasEnchants() ? meta.getEnchants() : null);
            this.setItemFlags(new ArrayList<>(meta.getItemFlags()));
            this.setHideTooltip(meta.isHideTooltip());
            this.setHideTooltip(meta.isHideTooltip());
            this.setUnbreakable(meta.isUnbreakable());
            this.setUnbreakable(meta.hasEnchantmentGlintOverride() ? meta.getEnchantmentGlintOverride() : null);
            this.setMaxStackSize(meta.hasMaxStackSize() ? meta.getMaxStackSize() : null);
            this.setRarity(meta.hasRarity() ? meta.getRarity() : null);
            this.setFood(meta.hasFood() ? meta.getFood() : null);
            this.setTool(meta.hasTool() ? meta.getTool() : null);
            this.setCustomTagContainer(meta.getCustomTagContainer());
            this.setDamage(meta instanceof Damageable && ((Damageable) meta).hasDamage() ? ((Damageable) meta).getDamage() : null);
            this.setMaxDamage(meta instanceof Damageable && ((Damageable) meta).hasMaxDamage() ? ((Damageable) meta).getMaxDamage() : null);
            this.setBasePotionType(meta instanceof PotionMeta && ((PotionMeta) meta).hasBasePotionType() ? ((PotionMeta) meta).getBasePotionType() : null);
            this.setCustomEffects(meta instanceof PotionMeta && ((PotionMeta) meta).hasCustomEffects() ? ((PotionMeta) meta).getCustomEffects() : null);
            this.setPotionColor(meta instanceof PotionMeta && ((PotionMeta) meta).hasColor() ? ((PotionMeta) meta).getColor() : null);
        }
    }

    public boolean hasDisplayName() {
        return this.getDisplayName() != null;
    }

    public boolean hasItemName() {
        return this.getItemName() != null;
    }

    public boolean hasLore() {
        return this.lore != null;
    }

    public List<String> getLore() {
        return List.copyOf(this.lore);
    }

    public void clearLore() {
        this.lore.clear();
    }

    public void setLore(int line, String lore) {
        this.lore.set(line - 1, lore);
    }

    public String getLore(int line) {
        return this.getLore().get(line);
    }

    public void addLore(int line, String... lore) {
        this.getLore().addAll(line - 1, Set.of(lore));
    }

    public void addLore(String... lore) {
        this.getLore().addAll(Set.of(lore));
    }

    public boolean hasCustomModelData() {
        return this.customModelData != null;
    }

    public boolean hasEnchantments() {
        return this.enchantments != null && !this.getEnchantments().isEmpty();
    }

    public boolean hasEnchantment(Enchantment enchantment) {
        return this.getEnchantments().containsKey(enchantment);
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        return this.getEnchantments().get(enchantment);
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return Map.copyOf(this.enchantments);
    }

    public boolean addEnchantment(Enchantment enchantment, int level, boolean override) {
        if (this.hasConflictingEnchant(enchantment) && !override) {
            return false;
        }
        this.enchantments.put(enchantment, level);
        return true;
    }

    public boolean removeEnchantment(Enchantment enchantment) {
        this.enchantments.remove(enchantment);
        return true;
    }

    public void removeEnchantments() {
        this.enchantments.clear();
    }

    public boolean hasConflictingEnchant(Enchantment enchantment) {
        return this.hasEnchantment(enchantment);
    }

    public void addItemFlags(ItemFlag... itemFlags) {
        this.itemFlags.addAll(Set.of(itemFlags));
    }

    public void removeItemFlags(ItemFlag... itemFlags) {
        this.itemFlags.removeAll(Set.of(itemFlags));
    }

    public boolean hasItemFlags() {
        return this.itemFlags != null;
    }

    public Set<ItemFlag> getItemFlags() {
        return Set.copyOf(this.itemFlags);
    }

    public boolean hasItemFlag(ItemFlag itemFlag) {
        return this.getItemFlags().contains(itemFlag);
    }

    public boolean hasHideTooltip() {
        return this.getHideTooltip() != null;
    }

    public boolean hasUnbreakable() {
        return this.getUnbreakable() != null;
    }

    public boolean hasEnchantmentGlintOverride() {
        return this.getEnchantmentGlintOverride() != null;
    }

    public boolean hasMaxStackSize() {
        return this.getMaxStackSize() != null;
    }

    public boolean hasRarity() {
        return this.getRarity() != null;
    }

    public boolean hasFood() {
        return this.getFood() != null;
    }

    public boolean hasTool() {
        return this.getTool() != null;
    }

    public boolean hasDamage() {
        return this.damage != null;
    }

    public boolean hasMaxDamage() {
        return this.maxDamage != null;
    }

    public boolean hasCustomEffects() {
        return this.customEffects != null;
    }

    public List<PotionEffect> getCustomEffects() {
        return List.copyOf(this.customEffects);
    }

    public void clearCustomEffects() {
        this.customEffects.clear();
    }

    public boolean hasCustomEffect(PotionEffectType type) {
        return this.customEffects.stream().anyMatch(effect -> effect.getType() == type);
    }

    public void addCustomEffect(PotionEffect effect, boolean override) {
        if (override || !this.hasCustomEffect(effect.getType())) {
            this.customEffects.add(effect);
        }
    }

    public void removeCustomEffect(PotionEffectType type) {
        this.clearCustomEffects();
        this.customEffects.addAll(this.customEffects.stream().filter(effect -> effect.getType() != type).collect(Collectors.toSet()));
    }

    public boolean hasPotionColor() {
        return this.potionColor != null;
    }

    public boolean hasBasePotionType() {
        return this.basePotionType != null;
    }
}