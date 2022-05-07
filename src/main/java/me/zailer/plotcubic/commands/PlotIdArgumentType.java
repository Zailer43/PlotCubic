package me.zailer.plotcubic.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.zailer.plotcubic.plot.PlotID;

import java.util.Arrays;
import java.util.Collection;

public class PlotIdArgumentType implements ArgumentType<PlotID> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0;0", "20;30", "8;-13", "-5;24", "-14;-3");
    private static final SimpleCommandExceptionType INVALID_PLOT_ID_FORMAT = new SimpleCommandExceptionType(new LiteralMessage("Invalid plotId format\nExamples: " + String.join(", ", EXAMPLES)));

    @Override
    public PlotID parse(final StringReader reader) throws CommandSyntaxException {
        String input = readPlotId(reader);
        if (!PlotID.isValid(input))
            throw INVALID_PLOT_ID_FORMAT.createWithContext(reader);

        return PlotID.of(input);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static String readPlotId(StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowedCharacter(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    public static boolean isAllowedCharacter(final char c) {
        return c >= '0' && c <= '9' || c == '-' || c == PlotID.DELIMITER;
    }
}
