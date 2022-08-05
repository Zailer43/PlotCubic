package me.zailer.plotcubic.mixin;


//@Mixin(ArgumentTypes.class)
//public abstract class ArgumentTypesMixin {
//
//    @Shadow
//    public static <T extends ArgumentType<?>> void register(String id, Class<T> argClass, ArgumentSerializer<T> serializer) {
//    }
//
//    @Inject(method = "register()V", at = @At("RETURN"))
//    private static void register(CallbackInfo ci) {
//        register("plot_id", PlotIdArgumentType.class, new ConstantArgumentSerializer<>(PlotIdArgumentType::new));
//    }
//}
