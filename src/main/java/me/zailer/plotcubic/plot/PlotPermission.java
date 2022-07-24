package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;
import java.util.HashMap;

public class PlotPermission implements IBooleanOption {
    public static final HashMap<String, PlotPermission> PERMISSION_HASH_MAP = new HashMap<>();
    public static final PlotPermission BREAK_BLOCKS = new PlotPermission("break_blocks", Items.IRON_PICKAXE, true);
    public static final PlotPermission DAMAGE_ENTITIES = new PlotPermission("damage_entities", Items.IRON_SWORD, true);
    public static final PlotPermission DESTROY_CONTAINER = new PlotPermission("destroy_container", Items.CHEST);
    public static final PlotPermission FILL_MAP = new PlotPermission("fill_map", Items.MAP);
    public static final PlotPermission OPEN_CONTAINER = new PlotPermission("open_container", Items.CHEST);
    public static final PlotPermission PLACE_BLOCKS = new PlotPermission("place_blocks", Items.GRASS_BLOCK);
    public static final PlotPermission SPAWN_ENTITIES = new PlotPermission("spawn_entities", Items.BAT_SPAWN_EGG);
    public static final PlotPermission PLACE_EXPLOSIVES = new PlotPermission("place_explosives", Items.TNT);
    public static final PlotPermission PLACE_FLUIDS = new PlotPermission("place_fluids", Items.WATER_BUCKET);
    public static final PlotPermission SLEEP = new PlotPermission("sleep", Items.RED_BED);
    public static final PlotPermission USE_BUTTONS = new PlotPermission("use_buttons", Items.STONE_BUTTON);
    public static final PlotPermission USE_LEVER = new PlotPermission("use_lever", Items.LEVER);
    //public static final PlotPermission USE_PRESSURE_PLATE = new PlotPermission("use_pressure_plate", Items.STONE_PRESSURE_PLATE);
    public static final PlotPermission USE_BOATS = new PlotPermission("use_boats", Items.OAK_BOAT);
    public static final PlotPermission USE_MINECART = new PlotPermission("use_minecart", Items.MINECART);
    //public static final PlotPermission PERMISSIONS_WITH_OWNER_OFFLINE = new PlotPermission("Always", null),
    //public static final PlotPermission PERMISSIONS_WITH_OWNER_ONLINE = new PlotPermission("Only when I'm online", null);
    public static final String BASE_TRANSLATION_KEY = "text.plotcubic.permissions.";
    public static final String BASE_PERMISSION_KEY = "plotcubic.command.trust.permissions.";
    private final String id;
    private final Item item;
    private final boolean hideAttributes;

    public PlotPermission(String id, Item item) {
        this(id, item, false);
    }

    public PlotPermission(String id, Item item, boolean hideAttributes) {
        this.id = id;
        this.item = item;
        this.hideAttributes = hideAttributes;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(BASE_TRANSLATION_KEY + this.id);
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

    public static void register() {
        register(BREAK_BLOCKS);
        register(DAMAGE_ENTITIES);
        register(DESTROY_CONTAINER);
        register(FILL_MAP);
        register(OPEN_CONTAINER);
        register(PLACE_BLOCKS);
        register(SPAWN_ENTITIES);
        register(PLACE_EXPLOSIVES);
        register(PLACE_FLUIDS);
        register(SLEEP);
        register(USE_BUTTONS);
        register(USE_LEVER);
        register(USE_BOATS);
        register(USE_MINECART);
    }

    public static void register(PlotPermission permission) {
        PERMISSION_HASH_MAP.put(permission.id, permission);
    }

}
