package coolclk.escape.command.tree;

import lombok.Getter;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.*;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class TreeTabExecutor implements TabExecutor {
    private final List<TreeTabExecutor> branches = new ArrayList<>();
    @Getter
    private final String label;
    private String permission;

    public TreeTabExecutor(String label, String permission, Collection<TreeTabExecutor> branches) {
        this.label = label;
        this.permission = permission;
        this.branches.addAll(branches);
    }

    public TreeTabExecutor(String label, String permission, TreeTabExecutor... branches) {
        this(label, permission, List.of(branches));
    }

    public TreeTabExecutor(String label, TreeTabExecutor... branches) {
        this(label, null, List.of(branches));
    }

    public TreeTabExecutor() {
        this(noneLabel());
    }

    protected static String noneLabel() {
        return null;
    }

    protected String getPermission() {
        return this.permission;
    }

    protected void setPermission(String permission) {
        this.permission = permission;
    }

    public TreeTabExecutor growBranch(TreeTabExecutor branch) {
        this.branches.add(branch);
        return this;
    }

    public TreeTabExecutor clearBranches() {
        this.branches.clear();
        return this;
    }

    public List<TreeTabExecutor> getBranches() {
        return List.copyOf(this.branches);
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        List<String> labels = new ArrayList<>();
        this.getBranches().forEach(branch -> labels.add(branch.getLabel()));
        return List.copyOf(labels.stream().filter(element -> element.startsWith(arguments[arguments.length - 1])).collect(Collectors.toSet()));
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        return false;
    }

    public boolean onPermissionDenied(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        return false;
    }

    public static TreeTabExecutor branch(CommandExecutor commandExecutor) {
        return branch(noneLabel(), commandExecutor);
    }

    public static TreeTabExecutor branch(TabExecutor executor) {
        return branch(noneLabel(), executor);
    }

    public static TreeTabExecutor branch(CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return branch(noneLabel(), commandExecutor, tabCompleter);
    }

    public static TreeTabExecutor branch(String label, CommandExecutor commandExecutor) {
        return new TreeTabExecutor(label) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return commandExecutor.onCommand(sender, command, label, arguments);
            }
        };
    }

    public static TreeTabExecutor branch(String label, TabExecutor executor) {
        return new TreeTabExecutor(label) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return executor.onCommand(sender, command, label, arguments);
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return executor.onTabComplete(sender, command, label, arguments);
            }
        };
    }

    public static TreeTabExecutor branch(String label, CommandExecutor commandExecutor, TabCompleter tabExecutor) {
        return branch(label, new TabExecutor() {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return commandExecutor.onCommand(sender, command, label, arguments);
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return tabExecutor.onTabComplete(sender, command, label, arguments);
            }
        });
    }
}
