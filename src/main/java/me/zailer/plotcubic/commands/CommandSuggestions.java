package me.zailer.plotcubic.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.plot.DeniedPlayer;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.TrustedPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestions {
    public static final SuggestionProvider<ServerCommandSource> ONLINE_PLAYER_SUGGESTION = ((context, builder) -> {
        List<String> usernamesList = context.getSource()
                .getServer()
                .getPlayerManager()
                .getPlayerList()
                .stream()
                .map(ServerPlayerEntity::getName)
                .map(Text::getString)
                .toList();

        String usernameInput = builder.getRemainingLowerCase();

        for (String username : usernamesList) {
            if (username.toLowerCase().contains(usernameInput))
                builder.suggest(username);
        }

        return CompletableFuture.completedFuture(builder.build());
    });

    public static final SuggestionProvider<ServerCommandSource> REMOVE_PLAYER_SUGGESTION = ((context, builder) -> {
        List<String> usernamesList = new ArrayList<>();

        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null)
            return CompletableFuture.completedFuture(builder.build());
        PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());
        if (plotId == null)
            return CompletableFuture.completedFuture(builder.build());

        Plot plot = Plot.getLoadedPlot(plotId);
        if (plot == null || !plot.isOwner(player))
            return CompletableFuture.completedFuture(builder.build());

        String usernameInput = builder.getRemainingLowerCase();

        usernamesList.addAll(plot.getDeniedPlayers().stream().map(DeniedPlayer::username).toList());
        usernamesList.addAll(plot.getTrusted().stream().map(TrustedPlayer::username).toList());

        for (String username : usernamesList) {
            if (username.toLowerCase().contains(usernameInput))
                builder.suggest(username);
        }

        return CompletableFuture.completedFuture(builder.build());
    });

    public static final SuggestionProvider<ServerCommandSource> GAME_MODE_SUGGESTION = ((context, builder) -> {
        List<String> gameModeList = new ArrayList<>();
        String usernameInput = builder.getRemainingLowerCase();
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        for (var gameMode : GameMode.values()) {
            if (Permissions.check(player,"plotcubic.command.gamemode." + gameMode.getName()))
                gameModeList.add(gameMode.getName());
        }

        for (String gameMode : gameModeList) {
            if (gameMode.toLowerCase().contains(usernameInput))
                builder.suggest(gameMode);
        }

        return CompletableFuture.completedFuture(builder.build());
    });
}
