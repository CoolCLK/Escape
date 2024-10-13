package coolclk.escape.command.tree;

import org.bukkit.command.*;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("unused")
public class BranchTabExecutor extends TreeTabExecutor {
    public BranchTabExecutor(String label, String permission, TreeTabExecutor branch) {
        super(label);
        this.setPermission(permission);
        this.growBranch(branch);
    }

    public BranchTabExecutor(String label, TreeTabExecutor branch) {
        this(label, null, branch);
    }

    public BranchTabExecutor(String label) {
        super(label);
    }

    public BranchTabExecutor(TreeTabExecutor branch) {
        this();
        this.growBranch(branch);
    }

    public BranchTabExecutor() {
        this(noneLabel());
    }

    public TreeTabExecutor getBranch() {
        return this.getBranches().getFirst();
    }

    @Override
    public TreeTabExecutor growBranch(TreeTabExecutor branch) {
        if (!this.getBranches().isEmpty()) {
            this.clearBranches();
        }
        return super.growBranch(branch);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        return super.onCommand(sender, command, label, arguments);
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        return List.of();
    }

    public static BranchTabExecutor branch(CommandExecutor commandExecutor) {
        return new BranchTabExecutor() {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return commandExecutor.onCommand(sender, command, label, arguments);
            }
        };
    }

    public static BranchTabExecutor branch(TabExecutor executor) {
        return branch(noneLabel(), executor);
    }

    public static BranchTabExecutor branch(CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return branch(noneLabel(), commandExecutor, tabCompleter);
    }

    public static BranchTabExecutor branch(TabExecutor executor, TreeTabExecutor branch) {
        return branch(noneLabel(), executor, branch);
    }

    public static BranchTabExecutor branch(CommandExecutor commandExecutor, TabCompleter tabCompleter, TreeTabExecutor branch) {
        return branch(noneLabel(), commandExecutor, tabCompleter, branch);
    }

    public static BranchTabExecutor branch(String label, CommandExecutor commandExecutor) {
        return new BranchTabExecutor(label) {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return commandExecutor.onCommand(sender, command, label, arguments);
            }
        };
    }

    public static BranchTabExecutor branch(String label, TabExecutor executor) {
        return new BranchTabExecutor(label) {
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

    public static BranchTabExecutor branch(String label, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return branch(new TabExecutor() {
            @Override
            public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return commandExecutor.onCommand(sender, command, label, arguments);
            }

            @Override
            public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
                return tabCompleter.onTabComplete(sender, command, label, arguments);
            }
        });
    }

    public static BranchTabExecutor branch(String label, TabExecutor executor, TreeTabExecutor branch) {
        return (BranchTabExecutor) branch(label, executor).growBranch(branch);
    }

    public static BranchTabExecutor branch(String label, CommandExecutor commandExecutor, TabCompleter tabCompleter, TreeTabExecutor branch) {
        return (BranchTabExecutor) branch(label, commandExecutor, tabCompleter).growBranch(branch);
    }
}
