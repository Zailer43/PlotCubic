package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class HomeCommand extends VisitCommand {
    @Override
    public String[] getAlias() {
        return new String[] { "home", "h" };
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require(this.getCommandPermission()))
                        .executes(this::execute)
                        .then(CommandManager.argument("NUMBER", IntegerArgumentType.integer(1))
                            .executes(this::execute)
                        )
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
        if (player == null)
            return 0;
        String playerUsername = player.getName().getString();

        Integer index = Utils.getArg(serverCommandSource, Integer.class, "NUMBER");
        MutableText message = this.visit(player, playerUsername, index == null ? 1 : index);
        player.sendMessage(message);

        return 1;
    }

    @Override
    public MutableText getOutBoundsErrorMsg(int plotCount) {
            return Text.translatable("error.plotcubic.plot.home.out_bounds", plotCount);
    }

    @Override
    public MutableText getThereAreNoPlotsMsg() {
        return Text.translatable("error.plotcubic.plot.home.there_are_no_plots", String.format("/%s %s", PlotCommand.COMMAND_ALIAS[0], new ClaimCommand().getAlias()[0]));
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.home";
    }
}
