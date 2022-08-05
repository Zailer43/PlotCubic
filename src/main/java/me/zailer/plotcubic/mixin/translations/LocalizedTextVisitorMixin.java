package me.zailer.plotcubic.mixin.translations;

import eu.pb4.placeholders.api.TextParserUtils;
import fr.catcore.server.translations.api.text.LocalizedTextVisitor;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalizedTextVisitor.class)
public interface LocalizedTextVisitorMixin {

    @Shadow
    void accept(MutableText text);

    /**
     * @author Zailer43
     * @reason add placeholder formatting
     */
    @Overwrite
    default void acceptLiteral(String string, Style style) {
        MutableText text = (MutableText) TextParserUtils.formatText(string);
        this.accept(text.fillStyle(style));
    }
}
