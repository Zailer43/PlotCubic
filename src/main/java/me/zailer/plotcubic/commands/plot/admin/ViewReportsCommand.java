package me.zailer.plotcubic.commands.plot.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.gui.ReportGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ViewReportsCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"reports"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .executes(this::execute)
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();

            new ReportGui().openViewReports(player);
        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.admin.view_reports";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }
}
