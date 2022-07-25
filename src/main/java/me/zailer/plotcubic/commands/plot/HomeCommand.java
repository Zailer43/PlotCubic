package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

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
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            String playerUsername = player.getName().getString();

            Integer index = Utils.getArg(serverCommandSource, Integer.class, "NUMBER");
            TranslatableText message = this.visit(player, playerUsername, index == null ? 1 : index);
            MessageUtils.sendChatMessage(player, message);

            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public TranslatableText getOutBoundsErrorMsg(int plotCount) {
            return new TranslatableText("error.plotcubic.plot.home.out_bounds", plotCount);
    }

    @Override
    public TranslatableText getThereAreNoPlotsMsg() {
        return new TranslatableText("error.plotcubic.plot.home.there_are_no_plots", String.format("/%s %s", PlotCommand.COMMAND_ALIAS[0], new ClaimCommand().getAlias()[0]));
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.home";
    }
}
