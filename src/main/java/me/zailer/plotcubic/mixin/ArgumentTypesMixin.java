package me.zailer.plotcubic.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import me.zailer.plotcubic.commands.PlotIdArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArgumentTypes.class)
public abstract class ArgumentTypesMixin {

    @Shadow
    public static <T extends ArgumentType<?>> void register(String id, Class<T> argClass, ArgumentSerializer<T> serializer) {
    }

    @Inject(method = "register()V", at = @At("RETURN"))
    private static void register(CallbackInfo ci) {
        register("plot_id", PlotIdArgumentType.class, new ConstantArgumentSerializer<>(PlotIdArgumentType::new));
    }
}
