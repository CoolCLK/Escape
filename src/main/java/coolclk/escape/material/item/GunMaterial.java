package coolclk.escape.material.item;

import coolclk.escape.Escape;
import coolclk.escape.common.MaterialManager;
import coolclk.escape.material.meta.CustomItemMeta;
import coolclk.escape.material.meta.item.GunItemMeta;
import coolclk.escape.material.CustomItemMaterial;
import de.tr7zw.nbtapi.NBT;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class GunMaterial extends CustomItemMaterial<GunItemMeta> {
    public GunMaterial() {
        super("gun", Material.FISHING_ROD);
    }

    @Nonnull
    @Override
    public GunItemMeta getItemMeta(ItemStack itemStack) {
        return new GunItemMeta(itemStack);
    }

    @Nonnull
    @Override
    public GunItemMeta getDefaultItemMeta() {
        GunItemMeta meta = new GunItemMeta();
        meta.setCustomModelData(12340001);
        meta.setReloadingBullet(false);
        meta.setDisplayName("枪");
        meta.setMaxDamage(30);
        meta.setDamage(meta.getMaxDamage());
        meta.setMaxBulletCount(30);
        meta.setBulletCount(meta.getMaxBulletCount());
        meta.setLore(List.of("§7子弹: " + meta.getBulletCount() + "/" + meta.getMaxBulletCount()));
        return meta;
    }

    @Override
    public void applyItemMeta(ItemStack itemStack, GunItemMeta customItemMeta) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0) {
            return;
        }
        customItemMeta.setMaxDamage(customItemMeta.getMaxBulletCount());
        customItemMeta.setDamage(customItemMeta.getMaxBulletCount() - customItemMeta.getBulletCount());
        customItemMeta.setLore(List.of("§7子弹: " + customItemMeta.getBulletCount() + "/" + customItemMeta.getMaxBulletCount()));
        super.applyItemMeta(itemStack, customItemMeta);
        NBT.modify(itemStack, nbt -> {
            nbt.setBoolean(CustomItemMeta.getKey("ReloadingBullet"), customItemMeta.isReloadingBullet());
            nbt.setInteger(CustomItemMeta.getKey("BulletCount"), customItemMeta.getBulletCount());
            nbt.setInteger(CustomItemMeta.getKey("MaxBulletCount"), customItemMeta.getMaxBulletCount());
        });
    }

    @Override
    public InteractResult interact(@Nonnull Player player, @Nonnull Action action, @Nullable ItemStack itemStack, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace, @Nullable EquipmentSlot hand, @Nullable Vector clickedPosition) {
        GunItemMeta meta = new GunItemMeta(itemStack);
        if (clickedBlock != null && (clickedBlock.isPassable() || clickedBlock.isLiquid())) {
            action = Action.RIGHT_CLICK_AIR;
        }
        switch (action) {
                case LEFT_CLICK_BLOCK: {
                    meta.setReloadingBullet(true);
                    if (!meta.hasBulletCount()) {
                        meta.setBulletCount(0);
                    }
                    this.applyItemMeta(itemStack, meta);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            GunItemMeta meta = new GunItemMeta(itemStack);
                            if (meta.getBulletCount() >= meta.getMaxBulletCount() || !MaterialManager.getItem(BulletMaterial.class).containsMaterial(player.getInventory())) {
                                meta.setReloadingBullet(false);
                                applyItemMeta(itemStack, meta);
                                this.cancel();
                                return;
                            }
                            if (!meta.isReloadingBullet()) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c填充弹药被打断"));
                                this.cancel();
                                return;
                            }
                            Arrays.stream(player.getInventory().getContents()).filter(stack -> Objects.equals(CustomItemMeta.getId(stack), MaterialManager.getItem(BulletMaterial.class).id())).findFirst().ifPresent(stack -> stack.setAmount(stack.getAmount() - 1));
                            meta.setBulletCount(meta.getBulletCount() + 1);
                            applyItemMeta(itemStack, meta);
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§e填充弹药中 §f(§b" + meta.getBulletCount() + "§f/" + meta.getMaxBulletCount() + "§f)"));
                        }
                    }.runTaskTimer(Escape.instance(), 0, 1L);
                    break;
                }
                case RIGHT_CLICK_AIR: {
                    meta.setReloadingBullet(false);
                    this.applyItemMeta(itemStack, meta);
                    if (meta.getBulletCount() > 0) {
                        meta.setBulletCount(meta.getBulletCount() - 1);
                        this.applyItemMeta(itemStack, meta);
                        player.getWorld().playEffect(player.getLocation(), Effect.BOW_FIRE, null);

                        Location startLocation = player.getEyeLocation();
                        double yaw = Math.toRadians(startLocation.getYaw() >= 110 ? 20 - startLocation.getYaw() : startLocation.getYaw() + 90), pitch = Math.toRadians(startLocation.getPitch());
                        Location bulletLocation = startLocation.clone();
                        AtomicReference<Float> speed = new AtomicReference<>(0.25F);
                        AtomicReference<Long> age = new AtomicReference<>(0L);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (float i = 0; i < 5; i += speed.get()) {
                                    bulletLocation.add(speed.get() * Math.cos(yaw), speed.get() * Math.sin(-pitch) - 0.01F, speed.get() * Math.sin(yaw));
                                    player.getWorld().spawnParticle(Particle.END_ROD, bulletLocation, 1, 0, 0, 0, 0, null, true);
                                    Block blockAt = player.getWorld().getBlockAt(bulletLocation);
                                    if (bulletLocation.getY() < 0 || !(blockAt.isEmpty() || blockAt.isLiquid() || blockAt.isPassable())) {
                                        this.cancel();
                                        return;
                                    }
                                    Collection<Entity> targets = player.getWorld().getNearbyEntities(bulletLocation, 0.5, 0.5, 0.5, entity -> entity instanceof Damageable && entity != player);
                                    if (!targets.isEmpty()) {
                                        for (Entity entity : targets) {
                                            LivingEntity target = (LivingEntity) entity;
                                            target.damage(meta.getBulletDamage());
                                        }
                                        this.cancel();
                                    }
                                    speed.set(speed.get() * 0.9999F);
                                }
                                age.set(age.get() + 1);
                                if (age.get() >= 100L) {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Escape.instance(), 0, 1L);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        InteractResult result = new InteractResult();
        result.setCancelled(true);
        return result;
    }

    @Override
    public void tick(@Nonnull Player player, @Nonnull ItemStack itemStack, @Nullable EquipmentSlot hand) {
        if (hand == EquipmentSlot.HAND && player.isSneaking()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1, 5, false, false, false));
        }
    }
}
