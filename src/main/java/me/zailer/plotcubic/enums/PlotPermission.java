package me.zailer.plotcubic.enums;

import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;

public enum PlotPermission implements IBooleanOption {
    BREAK_BLOCKS("break_blocks", Items.IRON_PICKAXE, true),
    DAMAGE_ENTITIES("damage_entities", Items.IRON_SWORD, true),
    DESTROY_CONTAINER("destroy_container", Items.CHEST),
    FILL_MAP("fill_map", Items.MAP),
    OPEN_CONTAINER("open_container", Items.CHEST),
    PLACE_BLOCKS("place_blocks", Items.GRASS_BLOCK),
    SPAWN_ENTITIES("spawn_entities", Items.BAT_SPAWN_EGG),
    PLACE_EXPLOSIVES("place_explosives", Items.TNT),
    PLACE_FLUIDS("place_fluids", Items.WATER_BUCKET),
    SLEEP("sleep", Items.RED_BED),
    USE_BUTTONS("use_buttons", Items.STONE_BUTTON),
    USE_LEVER("use_lever", Items.LEVER),
    USE_PRESSURE_PLATE("use_pressure_plate", Items.STONE_PRESSURE_PLATE),
    USE_BOATS("use_boats", Items.OAK_BOAT),
    USE_MINECART("use_minecart", Items.MINECART);
//    PERMISSIONS_WITH_OWNER_OFFLINE("Always", null),
//    PERMISSIONS_WITH_OWNER_ONLINE("Only when I'm online", null);

    private static final String BASE_TRANSLATION_KEY = "text.plotcubic.permissions.";
    private final String translationKey;
    private final Item item;
    private final boolean hideAttributes;

    PlotPermission(String translationKey, Item item) {
        this(translationKey, item, false);
    }

    PlotPermission(String translationKey, Item item, boolean hideAttributes) {
        this.translationKey = translationKey;
        this.item = item;
        this.hideAttributes = hideAttributes;
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(BASE_TRANSLATION_KEY + this.translationKey);
    }

    @Override
    public Item getItem() {
        return this.item;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public boolean isHideAttributes() {
        return this.hideAttributes;
    }

    @Override
    public boolean hasHeadValue() {
        return false;
    }

    @Override
    @Nullable
    public String getHeadValue() {
        return null;
    }
}
