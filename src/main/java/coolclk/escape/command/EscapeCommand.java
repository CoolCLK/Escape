package coolclk.escape.command;

import coolclk.escape.Escape;
import coolclk.escape.command.tree.BranchTabExecutor;
import coolclk.escape.common.GameManager;
import coolclk.escape.common.MaterialManager;
import coolclk.escape.common.TreasureManager;
import coolclk.escape.material.meta.CustomItemMeta;
import coolclk.escape.material.CustomItemMaterial;
import coolclk.escape.command.tree.RootTabExecutor;
import coolclk.escape.command.tree.TreeTabExecutor;
import coolclk.escape.material.meta.block.TreasureBlockMeta;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EscapeCommand extends RootTabExecutor {
    public void register() {
        PluginCommand pluginCommand = Bukkit.getPluginCommand(this.getLabel());
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
    }

    public void unregister() {
        PluginCommand pluginCommand = Bukkit.getPluginCommand(this.getLabel());
        if (pluginCommand != null) {
            if (pluginCommand.getExecutor() instanceof EscapeCommand) {
                pluginCommand.setExecutor(null);
            }
            if (pluginCommand.getTabCompleter() instanceof EscapeCommand) {
                pluginCommand.setTabCompleter(null);
            }
        }
    }

    /* --- register sub commands start --- */

    public EscapeCommand(String name) {
        super(name);
        this.growBranch(this.gameCommand());
        this.growBranch(this.itemCommand());
        this.growBranch(new BranchTabExecutor("reload") {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                TreasureManager.reloadLootTables();
                return true;
            }
        });
    }

    @Override
    public boolean onRootCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        StringBuilder context = new StringBuilder(label);
        for (String argument : arguments) {
            context.append(" ").append(argument);
        }
        sender.spigot().sendMessage(Message.unknownCommand(context.toString(), context.toString()));
        return false;
    }

    private TreeTabExecutor itemCommand() {
        return new TreeTabExecutor("item", this.itemGiveCommand(), this.itemClearCommand()) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                StringBuilder context = new StringBuilder(label);
                for (String argument : arguments) {
                    context.append(" ").append(argument);
                }
                sender.spigot().sendMessage(Message.unknownCommand(context.toString(), "item"));
                return false;
            }
        };
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private TreeTabExecutor itemGiveCommand() {
        TreeTabExecutor itemGiveCount = new BranchTabExecutor() {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return giveItem(sender, arguments[arguments.length - 3], arguments[arguments.length - 2], arguments[arguments.length - 1]);
            }
        };
        TreeTabExecutor itemGiveItem = new BranchTabExecutor(itemGiveCount) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return giveItem(sender, arguments[arguments.length - 2], arguments[arguments.length - 1], "");
            }
        };
        TreeTabExecutor itemGiveTargets = new BranchTabExecutor(itemGiveItem) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return unknownCommand(sender, label, arguments);
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                List<String> itemsId = new ArrayList<>();
                MaterialManager.itemMaterials().forEach(item -> itemsId.add(item.id()));
                return List.copyOf(itemsId).stream().filter(selection -> selection.startsWith(arguments[arguments.length - 1])).collect(Collectors.toList());
            }
        };
        return new BranchTabExecutor("give", "escape.command.give", itemGiveTargets) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return unknownCommand(sender, label, arguments);
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                List<String> playersName = new ArrayList<>();
                Bukkit.getServer().getOnlinePlayers().forEach(player -> playersName.add(player.getName()));
                return List.copyOf(playersName).stream().filter(selection -> selection.startsWith(arguments[arguments.length - 1])).collect(Collectors.toList());
            }

            @Override
            public boolean onPermissionDenied(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                StringBuilder context = new StringBuilder(label);
                for (String argument : arguments) {
                    context.append(" ").append(argument);
                }
                sender.spigot().sendMessage(Message.unknownCommand(context.toString(), "give"));
                return false;
            }
        };
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private TreeTabExecutor itemClearCommand() {
        TreeTabExecutor count = new BranchTabExecutor() {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return clearItem(sender, arguments[arguments.length - 3], arguments[arguments.length - 2], arguments[arguments.length - 1]);
            }
        };
        TreeTabExecutor item = new BranchTabExecutor(count) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return clearItem(sender, arguments[arguments.length - 2], arguments[arguments.length - 1], "");
            }
        };
        TreeTabExecutor targets = new BranchTabExecutor(item) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return clearItem(sender, arguments[arguments.length - 1], "*", "");
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                List<String> itemsId = new ArrayList<>(List.of("*"));
                MaterialManager.itemMaterials().forEach(item -> itemsId.add(item.id()));
                return List.copyOf(itemsId).stream().filter(selection -> selection.startsWith(arguments[arguments.length - 1])).collect(Collectors.toList());
            }
        };
        return new BranchTabExecutor("clear", "escape.command.clear", targets) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return clearItem(sender, "", "*", "");
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                List<String> playersName = new ArrayList<>();
                Bukkit.getServer().getOnlinePlayers().forEach(player -> playersName.add(player.getName()));
                return List.copyOf(playersName).stream().filter(selection -> selection.startsWith(arguments[arguments.length - 1])).collect(Collectors.toList());
            }

            @Override
            public boolean onPermissionDenied(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                StringBuilder context = new StringBuilder(label);
                for (String argument : arguments) {
                    context.append(" ").append(argument);
                }
                sender.spigot().sendMessage(Message.unknownCommand(context.toString(), "clear"));
                return false;
            }
        };
    }

    private TreeTabExecutor gameCommand() {
        return new TreeTabExecutor("game", new BranchTabExecutor(new TreeTabExecutor("treasure", "escape.command.treasure", new TreeTabExecutor("generate") {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                String game = arguments[arguments.length - 3];
                if (GameManager.hasGame(game)) {
                    AtomicInteger success = new AtomicInteger();
                    for (GameManager.TreasureConfiguration element : GameManager.gameTreasures(game)) {
                        Location location = element.getLocation();
                        Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(location);
                        element.getType().applyBlockMeta(block, new TreasureBlockMeta(element.getType().createItemStack()));
                        success.addAndGet(1);
                    }
                    sender.spigot().sendMessage(new TextComponent("成功生成" + success.get() + "个资源箱"));
                } else {
                    sender.spigot().sendMessage(new TextComponent("§c该游戏不存在"));
                }
                return true;
            }
        }) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return unknownCommand(sender, label, arguments);
            }

            @Override
            public boolean onPermissionDenied(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                StringBuilder context = new StringBuilder(label);
                for (String argument : arguments) {
                    context.append(" ").append(argument);
                }
                sender.spigot().sendMessage(Message.unknownCommand(context.toString(), "treasure"));
                return false;
            }
        }) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return unknownCommand(sender, label, arguments);
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return Collections.singletonList("treasure");
            }
        }) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                StringBuilder context = new StringBuilder(label);
                for (String argument : arguments) {
                    context.append(" ").append(argument);
                }
                sender.spigot().sendMessage(Message.unknownCommand(context.toString(), "game"));
                return false;
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return GameManager.gameList();
            }
        };
    }

    private boolean unknownCommand(CommandSender sender, String label, String[] arguments) {
        StringBuilder command = new StringBuilder(label);
        for (String argument : arguments) {
            command.append(" ").append(argument);
        }
        sender.spigot().sendMessage(Message.unknownCommand(command.toString()));
        return false;
    }

    private boolean giveItem(CommandSender sender, String targetName, String itemName, String countName) {
        final String context = "...give " + targetName + " " + itemName + (countName.isEmpty() ? "" : " " + countName);
        Player target = Bukkit.getServer().getOnlinePlayers().stream().filter(player -> player.getName().equals(targetName)).findAny().orElse(null);
        if (target == null) {
            sender.spigot().sendMessage(Message.entityNotFoundPlayer());
            return false;
        }
        CustomItemMaterial<?> item = MaterialManager.itemMaterials().stream().filter(customItem -> Objects.equals(customItem.id(), itemName)).findAny().orElse(null);
        if (item == null) {
            sender.spigot().sendMessage(Message.invalidItemId(context, itemName));
            return false;
        }
        try {
            int count = countName.isEmpty() ? 1 : Integer.parseInt(countName);
            if (count < 1) {
                sender.spigot().sendMessage(Message.integerLow(1, count, context));
                return false;
            }
            sender.spigot().sendMessage(Message.giveSuccessSingle(this.giveItem(target, item, count), targetName));
            return true;
        } catch (NumberFormatException ignored) {
            sender.spigot().sendMessage(Message.intExpected(context, countName));
        }
        return false;
    }

    private ItemStack giveItem(InventoryHolder target, CustomItemMaterial<?> item, int count) {
        int max = item.getDefaultItemMeta().hasMaxStackSize() ? item.getDefaultItemMeta().getMaxStackSize() : item.currentMaterial().getMaxStackSize();
        for (int i = 0; i < count;) {
            int size = Math.min(count - i, max);
            ItemStack stack = item.createItemStack(size);
            target.getInventory().addItem(stack);
            i += size;
        }
        return item.createItemStack(count);
    }

    private boolean clearItem(CommandSender sender, String targetName, String itemName, String countName) {
        final String context = "...clear" + (targetName.isEmpty() ? "" : " " + targetName) + (itemName.isEmpty() ? "" : " " + itemName) + (countName.isEmpty() ? "" : " " + countName);
        Player target = Bukkit.getServer().getOnlinePlayers().stream().filter(player -> player.getName().equals(targetName)).findAny().orElse(null);
        CustomItemMaterial<?> material = MaterialManager.get(itemName);
        AtomicInteger maxCount = new AtomicInteger(Integer.MAX_VALUE);

        if (targetName.isEmpty()) {
            if (sender instanceof Player) {
                return this.clearItem(sender, sender.getName(), "*", "");
            }
            sender.spigot().sendMessage(Message.requiresPlayer());
            return false;
        } else if (target == null) {
            sender.spigot().sendMessage(Message.entityNotFoundPlayer());
            return false;
        }

        if (!countName.isEmpty()) {
            try {
                int parse = Integer.parseInt(countName);
                if (parse > 0) {
                    maxCount.set(parse);
                } else {
                    sender.spigot().sendMessage(Message.integerLow(1, parse, context));
                    return false;
                }
            } catch (NumberFormatException ignored) {
                sender.spigot().sendMessage(Message.intExpected(context, countName));
                return false;
            }
        }

        boolean matchesAll = itemName.equals("*");
        if (material != null || matchesAll) {
            AtomicInteger count = new AtomicInteger();
            Arrays.stream(target.getInventory().getContents())
                    .filter(itemStack -> {
                        boolean all = matchesAll && CustomItemMeta.hasId(itemStack);
                        boolean single = material != null && Objects.equals(CustomItemMeta.getId(itemStack), material.id());
                        return all || single;
                    })
                    .collect(Collectors.toSet())
                    .forEach(itemStack -> {
                        while (count.get() < maxCount.get() && itemStack.getAmount() > 0) {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                            count.addAndGet(1);
                        }
                    });
            sender.spigot().sendMessage(Message.clearSuccessSingle(count.get(), targetName));
            return true;
        }
        sender.spigot().sendMessage(Message.invalidItemId(context, itemName));
        return false;
    }

    static class Message {
        public static BaseComponent[] unknownCommand(String context) {
            TranslatableComponent messageComponent = new TranslatableComponent("command.unknown.command");
            messageComponent.setColor(ChatColor.RED);
            TextComponent contextComponent = new TextComponent(context);
            contextComponent.setColor(ChatColor.GRAY);
            TranslatableComponent hereComponent = new TranslatableComponent("command.context.here");
            hereComponent.setItalic(true);
            hereComponent.setColor(ChatColor.RED);
            return new BaseComponent[]{messageComponent, new TextComponent("\n"), contextComponent, hereComponent};
        }

        public static BaseComponent[] unknownCommand(String context, String wrong) {
            TranslatableComponent messageComponent = new TranslatableComponent("command.unknown.command");
            messageComponent.setColor(ChatColor.RED);
            TextComponent contextComponent = new TextComponent(context.substring(0, context.startsWith(wrong) ? 0 : context.lastIndexOf(wrong)));
            contextComponent.setColor(ChatColor.GRAY);
            TextComponent wContextComponent = new TextComponent(context.startsWith(wrong) ? context : context.substring(context.lastIndexOf(wrong)));
            wContextComponent.setUnderlined(true);
            wContextComponent.setColor(ChatColor.RED);
            TranslatableComponent hereComponent = new TranslatableComponent("command.context.here");
            hereComponent.setItalic(true);
            hereComponent.setColor(ChatColor.RED);
            return new BaseComponent[]{messageComponent, new TextComponent("\n"), contextComponent, wContextComponent, hereComponent};
        }

        public static BaseComponent[] invalidItemId(String context, String item) {
            TranslatableComponent messageComponent = new TranslatableComponent("argument.item.id.invalid");
            messageComponent.addWith(item);
            messageComponent.setColor(ChatColor.RED);
            TextComponent contextComponent = new TextComponent(context.substring(0, context.lastIndexOf(item)));
            contextComponent.setColor(ChatColor.GRAY);
            TextComponent wContextComponent = new TextComponent(context.substring(context.lastIndexOf(item)));
            wContextComponent.setUnderlined(true);
            wContextComponent.setColor(ChatColor.RED);
            TranslatableComponent hereComponent = new TranslatableComponent("command.context.here");
            hereComponent.setItalic(true);
            hereComponent.setColor(ChatColor.RED);
            return new BaseComponent[]{messageComponent, new TextComponent("\n"), contextComponent, wContextComponent, hereComponent};
        }

        public static BaseComponent[] intExpected(String context, String expected) {
            TranslatableComponent messageComponent = new TranslatableComponent("argument.item.id.invalid");
            messageComponent.setColor(ChatColor.RED);
            TextComponent contextComponent = new TextComponent(context.substring(0, context.length() - expected.length()));
            contextComponent.setColor(ChatColor.GRAY);
            TextComponent wContextComponent = new TextComponent(expected);
            wContextComponent.setUnderlined(true);
            wContextComponent.setColor(ChatColor.RED);
            TranslatableComponent hereComponent = new TranslatableComponent("command.context.here");
            hereComponent.setItalic(true);
            hereComponent.setColor(ChatColor.RED);
            return new BaseComponent[]{messageComponent, new TextComponent("\n"), contextComponent, wContextComponent, hereComponent};
        }

        public static BaseComponent[] integerLow(int value, int found, String context) {
            TranslatableComponent messageComponent = new TranslatableComponent("argument.integer.low");
            messageComponent.addWith(Integer.toString(value));
            messageComponent.addWith(Integer.toString(found));
            messageComponent.setColor(ChatColor.RED);
            TextComponent contextComponent = new TextComponent(context.substring(0, context.length() - Integer.toString(found).length()));
            contextComponent.setColor(ChatColor.GRAY);
            TextComponent wContextComponent = new TextComponent(Integer.toString(found));
            wContextComponent.setUnderlined(true);
            wContextComponent.setColor(ChatColor.RED);
            TranslatableComponent hereComponent = new TranslatableComponent("command.context.here");
            hereComponent.setItalic(true);
            hereComponent.setColor(ChatColor.RED);
            return new BaseComponent[]{messageComponent, new TextComponent("\n"), contextComponent, wContextComponent, hereComponent};
        }

        public static BaseComponent[] giveSuccessSingle(ItemStack itemStack, String selector) {
            TranslatableComponent messageComponent = new TranslatableComponent("commands.give.success.single");
            StringBuilder shownItem = new StringBuilder();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                String color;
                switch (itemMeta.hasRarity() ? itemMeta.getRarity() : null) {
                    case EPIC -> color = "§d";
                    case RARE -> color = "§e";
                    case UNCOMMON -> color = "§b";
                    case null, default -> color = "";
                }
                shownItem.append(color).append("[").append(itemMeta.getDisplayName()).append(color).append("]§r");
            }
            messageComponent.addWith(String.valueOf(itemStack.getAmount()));
            messageComponent.addWith(shownItem.toString());
            messageComponent.addWith(new SelectorComponent(selector));
            return new BaseComponent[]{messageComponent};
        }

        public static BaseComponent[] clearSuccessSingle(int itemsCount, String playerName) {
            TranslatableComponent messageComponent = new TranslatableComponent("commands.clear.success.single");
            messageComponent.addWith(String.valueOf(itemsCount));
            messageComponent.addWith(playerName);
            return new BaseComponent[]{messageComponent};
        }

        public static BaseComponent[] requiresPlayer() {
            TranslatableComponent messageComponent = new TranslatableComponent("permissions.requires.player");
            messageComponent.setColor(ChatColor.RED);
            return new BaseComponent[]{messageComponent};
        }

        public static BaseComponent[] entityNotFoundPlayer() {
            TranslatableComponent messageComponent = new TranslatableComponent("argument.entity.notfound.player");
            messageComponent.setColor(ChatColor.RED);
            return new BaseComponent[]{messageComponent};
        }
    }
}
