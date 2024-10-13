package coolclk.escape.command.tree;

import coolclk.escape.Escape;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class RootTabExecutor extends TreeTabExecutor {
    public RootTabExecutor(String label, String permission, Collection<TreeTabExecutor> branches) {
        super(label, permission, branches);
    }

    public RootTabExecutor(String label, String permission, TreeTabExecutor... branches) {
        this(label, permission, List.of(branches));
    }

    public RootTabExecutor(String label, Collection<TreeTabExecutor> branches) {
        this(label, null, branches);
    }

    public RootTabExecutor(String label, TreeTabExecutor... branches) {
        this(label, null, List.of(branches));
    }

    public boolean onRootCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        return super.onCommand(sender, command, label, arguments);
    }

    public List<String> onRootTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        return super.onTabComplete(sender, command, label, arguments);
    }

    @Override
    public final boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        if (!label.isEmpty() && !Objects.equals(label, this.getLabel())) {
            return false;
        }
        AtomicReference<TreeTabExecutor> ant = new AtomicReference<>(this);
        for (final String argument : arguments) {
            TreeTabExecutor lastAnt = ant.get();
            if (ant.get() instanceof BranchTabExecutor) {
                ant.set(((BranchTabExecutor) ant.get()).getBranch());
                if (ant.get().getPermission() != null && !ant.get().getPermission().isEmpty() && !sender.hasPermission(ant.get().getPermission())) {
                    if (!ant.get().onPermissionDenied(sender, command, label, arguments)) {
                        return false;
                    }
                }
            } else {
                ant.get().getBranches().stream().filter(branch -> Objects.equals(argument, branch.getLabel())).findAny().ifPresent(ant::set);
            }
            if (ant.get() == lastAnt) {
                ant.set(ant.get().getBranches().getFirst());
            }
        }
        if (ant.get() == this) {
            return this.onRootCommand(sender, command, label, arguments);
        }
        return ant.get().onCommand(sender, command, label, arguments);
    }

    @Override
    public final List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        if (!label.isEmpty() && !Objects.equals(label, this.getLabel())) {
            return List.of();
        }
        AtomicReference<TreeTabExecutor> ant = new AtomicReference<>(this);
        for (final String argument : Arrays.asList(arguments).subList(0, arguments.length - 1)) {
            TreeTabExecutor lastAnt = ant.get();
            if (ant.get() instanceof BranchTabExecutor) {
                ant.set(((BranchTabExecutor) ant.get()).getBranch());
            } else {
                ant.get().getBranches().stream().filter(branch -> Objects.equals(argument, branch.getLabel())).findAny().ifPresent(ant::set);
            }
            if (ant.get() == lastAnt) {
                ant.set(ant.get().getBranches().getFirst());
            }
        }
        if (ant.get() == this) {
            return this.onRootTabComplete(sender, command, label, arguments);
        }
        return ant.get().onTabComplete(sender, command, label, arguments);
    }
}
