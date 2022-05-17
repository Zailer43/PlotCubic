package me.zailer.plotcubic.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.zailer.plotcubic.commands.plot.*;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class PlotCommand {
    public static final String[] COMMAND_ALIAS = {"plot", "p", "p3"};
    public static final SubcommandAbstract[] SUB_COMMANDS = {
            new AdminCommand(),
            new ChatCommand(),
            new ChatStyleCommand(),
            new ClaimCommand(),
            new ClearCommand(),
            new DeleteCommand(),
            new DenyCommand(),
            new GameModeCommand(),
            new HelpCommand(),
            new HomeCommand(),
            new InfoCommand(),
            new RemoveCommand(),
            new ReportCommand(),
            new TrustedCommand(),
            new VisitCommand(),
            new TeleportCommand()
    };

    public static void register() {
        for (var alias : COMMAND_ALIAS) {


            LiteralArgumentBuilder<ServerCommandSource> command = literal(alias);

            for (var subCommand : SUB_COMMANDS)
                subCommand.apply(command);

            CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(command));

        }
    }
}
