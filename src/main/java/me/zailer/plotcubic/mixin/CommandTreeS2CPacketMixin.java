package me.zailer.plotcubic.mixin;


//@Mixin(CommandTreeS2CPacket.class)
//public class CommandTreeS2CPacketMixin {
//
//    @ModifyArg(
//            method = "writeNode(Lnet/minecraft/network/PacketByteBuf;Lcom/mojang/brigadier/tree/CommandNode;Ljava/util/Map;)V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/command/argument/ArgumentTypes;toPacket(Lnet/minecraft/network/PacketByteBuf;Lcom/mojang/brigadier/arguments/ArgumentType;)V"
//            ),
//            index = 1
//    )
//    private static ArgumentType<?> byClass(ArgumentType<?> type) {
//        return type instanceof PlotIdArgumentType ? StringArgumentType.greedyString() : type;
//    }
//}
