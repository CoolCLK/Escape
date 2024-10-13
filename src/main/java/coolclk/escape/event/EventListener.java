package coolclk.escape.event;

import coolclk.escape.Escape;
import coolclk.escape.common.MaterialManager;
import coolclk.escape.common.PlayerDataManager;
import coolclk.escape.material.CustomBlockMaterial;
import coolclk.escape.material.CustomItemMaterial;
import coolclk.escape.material.item.ConsumableMaterial;
import coolclk.escape.material.meta.CustomBlockMeta;
import coolclk.escape.material.meta.CustomItemMeta;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (MaterialManager.get(CustomItemMeta.getId(event.getItem())) instanceof ConsumableMaterial<?>) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerDeathEvent event) {
        PlayerDataManager.modifyPlayerData(event.getEntity(), playerData -> {
            playerData.setEnergy(0);
        });
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (this.itemInteract(event)) {
            this.blockInteract(event);
        }
    }

    private boolean itemInteract(PlayerInteractEvent event) {
        AtomicBoolean block = new AtomicBoolean(true);
        if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
            String id = CustomItemMeta.getId(event.getItem());
            MaterialManager.itemMaterials().stream().filter(material -> Objects.equals(material.id(), id)).findAny().ifPresent(item -> {
                if (item.currentMaterial() != event.getItem().getType()) {
                    return;
                }
                CustomItemMaterial.InteractResult result = item.interact(event.getPlayer(), event.getAction(), event.getItem(), event.getClickedBlock(), event.getBlockFace(), event.getHand(), event.getClickedPosition());
                if (result.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
                event.setUseItemInHand(result.getUseItemInHand());
                event.setUseInteractedBlock(result.getUseInteractedBlock());
                if (result.getUseInteractedBlock() == Event.Result.DENY) {
                    block.set(false);
                }
            });
        }
        return block.get();
    }

    private void blockInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.AIR) {
            String id = CustomBlockMeta.getId(event.getClickedBlock());
            MaterialManager.blockMaterials().stream().filter(material -> Objects.equals(material.id(), id)).findAny().ifPresent(block -> {
                if (block.currentMaterial() != event.getClickedBlock().getType()) {
                    return;
                }
                CustomItemMaterial.InteractResult result = block.beInteracted(event.getPlayer(), event.getAction(), event.getItem(), event.getClickedBlock(), event.getBlockFace(), event.getHand(), event.getClickedPosition());
                if (result.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
                event.setUseItemInHand(result.getUseItemInHand());
                event.setUseInteractedBlock(result.getUseInteractedBlock());
            });
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (CustomItemMeta.hasId(event.getItemInHand())) {
            CustomBlockMaterial<CustomBlockMeta, ?> material = MaterialManager.getBlock(CustomItemMeta.getId(event.getItemInHand()));
            if (event.getBlockPlaced().getType() == material.currentMaterial()) {
                NBT.modify(event.getBlockPlaced().getState(), root -> {
                    ReadWriteNBT components = root.getCompound("components");
                    if (components != null) {
                        ReadWriteNBT customData = components.getCompound("minecraft:custom_data");
                        if (customData != null) {;
                            customData.setString(CustomBlockMeta.getKey("id"), material.id());
                        }
                    }
                });
                material.applyBlockMeta(event.getBlockPlaced(), material.getBlockMeta(event.getItemInHand()));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.AIR) {
            String id = CustomBlockMeta.getId(event.getBlock());
            MaterialManager.blockMaterials().stream().filter(customItem -> Objects.equals(customItem.id(), id)).findAny().ifPresent(item -> {
                if (item.currentMaterial() != event.getBlock().getType()) {
                    return;
                }
                CustomBlockMaterial.BreakResult result = item.broken(event.getPlayer(), event.getBlock(), event.getExpToDrop());
                if (result.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
                if (result.getDropItems() != null) {
                    event.setDropItems(result.getDropItems());
                }
                if (result.getExpToDrop() != null) {
                    event.setExpToDrop(result.getExpToDrop());
                }
            });
        }
    }
}
