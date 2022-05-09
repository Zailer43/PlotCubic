package me.zailer.plotcubic.enums;

import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import javax.annotation.Nullable;

public enum PlotPermission implements IBooleanOption {
    BREAK_BLOCKS("Break blocks", Items.IRON_PICKAXE, true),
    DAMAGE_ENTITIES("Damage entities", Items.IRON_SWORD, true),
    DESTROY_CONTAINER("Destroy container", Items.CHEST),
    FILL_MAP("Fill map", Items.MAP),
    OPEN_CONTAINER("Open container", Items.CHEST),
    PLACE_BLOCKS("Place blocks", Items.GRASS_BLOCK),
    SPAWN_ENTITIES("Spawn entities", Items.BAT_SPAWN_EGG),
    PLACE_EXPLOSIVES("Place explosives", Items.TNT),
    PLACE_FLUIDS("Place fluids", Items.WATER_BUCKET),
    SLEEP("Sleep", Items.RED_BED),
    USE_BUTTONS("Use buttons", Items.STONE_BUTTON),
    USE_LEVER("Use lever", Items.LEVER),
    USE_PRESSURE_PLATE("Use pressure plate", Items.STONE_PRESSURE_PLATE),
    USE_BOATS("Use boats", Items.OAK_BOAT),
    USE_MINECART("Use minecart", Items.MINECART);
//    PERMISSIONS_WITH_OWNER_OFFLINE("Always", null),
//    PERMISSIONS_WITH_OWNER_ONLINE("Only when I'm online", null);

    private final String name;
    private final Item item;
    private final boolean hideAttributes;

    PlotPermission(String name, Item item) {
        this(name, item, false);
    }

    PlotPermission(String name, Item item, boolean hideAttributes) {
        this.name = name;
        this.item = item;
        this.hideAttributes = hideAttributes;
    }

    @Override
    public String getName() {
        return this.name;
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
