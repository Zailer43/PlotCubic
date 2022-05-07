package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.concurrent.CompletableFuture;

public class HelpCommand extends SubcommandAbstract {
    public static final SuggestionProvider<ServerCommandSource> SUB_COMMAND_SUGGESTION = HelpCommand::getSubCommandsSuggestion;

    @Override
    public String[] getAlias() {
        return new String[]{"help", "commands"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .executes(this::execute)
                        .then(CommandManager.argument("subcommand", StringArgumentType.word())
                                .suggests(SUB_COMMAND_SUGGESTION)
                                .executes(this::sendHelpMessage))
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();

            MessageUtils.sendChatMessage(player, new LiteralText("W I P"));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    protected String getHelpDetails() {
        return "Used to get information about commands, if you don't specify the subcommand it opens GUI with all subcommands";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    private int sendHelpMessage(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            String subcommandSelected = serverCommandSource.getArgument("subcommand", String.class);

            for (var subcommand : PlotCommand.SUB_COMMANDS) {
                for (var commandAlias : subcommand.getAlias()) {
                    if (commandAlias.equalsIgnoreCase(subcommandSelected)) {
                        this.sendHelpMessage(subcommand, player);
                        return 1;
                    }
                }
            }

            MessageUtils.sendChatMessage(player, MessageUtils.getError("Invalid subcommand").get());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void sendHelpMessage(SubcommandAbstract subcommand, ServerPlayerEntity player) {
        MessageUtils.sendChatMessage(player, subcommand.getHelpMessage());
    }

    private static CompletableFuture<Suggestions> getSubCommandsSuggestion(CommandContext<ServerCommandSource> source, SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();

        for (var subCommand : PlotCommand.SUB_COMMANDS) {
            for (var commandAlias : subCommand.getAlias()) {
                if (commandAlias.contains(input))
                    builder.suggest(commandAlias);
            }
        }

        return CompletableFuture.completedFuture(builder.build());
    }
}
