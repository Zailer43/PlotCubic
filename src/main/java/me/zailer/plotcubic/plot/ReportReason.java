package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record ReportReason(String id, int descriptionCount, Config.ItemConfig item) implements IBooleanOption {
    private static final String BASIC_TRANSLATION_KEY = "text.plotcubic.report_reason.";

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(BASIC_TRANSLATION_KEY + this.id + ".name");
    }

    @Override
    public List<Text> getDescription() {
        List<Text> descriptionList = new ArrayList<>();

        for (int i = 0; i != this.descriptionCount; i++)
            descriptionList.add(Text.translatable(BASIC_TRANSLATION_KEY + this.id + ".description." + i));

        return descriptionList;
    }

    @Override
    public Item getItem() {
        return Registry.ITEM.get(new Identifier(this.item.itemId()));
    }

    @Override
    public int getCount() {
        return this.item.count();
    }

    @Override
    public boolean isHideAttributes() {
        return this.item.hideAttributes();
    }

    @Override
    public boolean hasHeadValue() {
        return this.item.headValue() != null;
    }

    @Override
    public boolean hasGlow() {
        return this.item.glow();
    }

    @Nullable
    @Override
    public String getHeadValue() {
        return this.item.headValue();
    }

    public boolean isId(String id) {
        return this.id.equals(id);
    }

    public static ReportReason byId(String id, ReportReason defaultReturn) {
        ReportReason[] reportReasons = PlotCubic.getConfig().reportReasons();
        for (var reason : reportReasons) {
            if (reason.isId(id))
                return reason;
        }

        return defaultReturn;
    }
}