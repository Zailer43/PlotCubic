package me.zailer.plotcubic.config;

import me.zailer.plotcubic.plot.PlotChatStyle;
import me.zailer.plotcubic.plot.ReportReason;

public record Config(int CONFIG_VERSION_DONT_TOUCH_THIS, General general, PlotworldConfig PlotWorld,
                     Database database, PlotChatStyle[] plotChatStyles, CustomColors customColors,
                     ReportReason[] reportReasons) {

    public static final Config DEFAULT = new Config(
            1,
            new General(true,
                    true,
                    true,
                    new BlockPos(0, 0, 0),
                    new String[] { "armor_stand", "arrow", "boat", "chest_minecart", "egg", "end_crystal", "ender_pearl", "experience_bottle", "eye_of_ender", "firework_rocket", "fishing_bobber", "furnace_minecart", "glow_item_frame", "item", "item_frame", "minecart", "painting", "potion", "snowball", "spectral_arrow", "tnt", "trident" },
                    new String[] { "eye_of_ender", "item" }
            ),
            new PlotworldConfig(
                    8,
                    200,
                    -64,
                    320,
                    "minecraft:plains",
                    new BlockConfig("stone_slab", new BlockStateConfig[]{}),
                    new BlockConfig("deepslate_tile_slab", new BlockStateConfig[]{}),
                    new BlockConfig("deepslate_tiles", new BlockStateConfig[]{}),
                    new BlockConfig("white_concrete", new BlockStateConfig[]{}),
                    new Layer[]{
                            new Layer(1, new BlockConfig("barrier", new BlockStateConfig[]{})),
                            new Layer(49, new BlockConfig("stone", new BlockStateConfig[]{})),
                            new Layer(1, new BlockConfig("grass_block", new BlockStateConfig[]{}))
                    }
            ),
            new Database(
                    "h2",
                    "root",
                    "123",
                    "plotcubic"
            ),
            new PlotChatStyle[]{
                    new PlotChatStyle("frozen_berries", "Frozen Berries", "sweet_berries",
                            "<color:#595e60>[<color:#707370>%plot_id%<color:#595e60>] <color:#b0b7c0>%username% <color:#707370>» <color:#b0b7c0>%message%"),
                    new PlotChatStyle("window_tile", "Window Tide", "cyan_stained_glass_pane",
                            "<color:#41729f>[<color:#5885af>%plot_id%<color:#41729f>] <color:#c3e0e5>%username% <color:#5885af>» <color:#c3e0e5>%message%"),
                    new PlotChatStyle("rosy_flamingo", "Rosy Flamingo", "pink_dye",
                            "<color:#67595e>[<color:#a49393>%plot_id%<color:#67595e>] <color:#e8b4b8>%username% <color:#eed6d3>» <color:#e8b4b8>%message%"),
                    new PlotChatStyle("healthy_leaves", "Healthy Leaves", "azalea_leaves",
                            "<color:#3d550c>[<color:#81b622>%plot_id%<color:#3d550c>] <color:#ecf87f>%username% <color:#81b622>» <color:#ecf87f>%message%"),
                    new PlotChatStyle("candy_hearts", "Candy Hearts", "cake",
                            "<color:#fceca5>[<color:#fec8a7>%plot_id%<color:#fceca5>] <color:#b5eecb>%username% <color:#cbc7fc>» <color:#b5eecb>%message%"),
                    new PlotChatStyle("sweeter_love", "Sweeter Love", "red_dye",
                            "<color:#838bc2>[<color:#a58cb3>%plot_id%<color:#838bc2>] <color:#d9a1a0>%username% <color:#eae7fa>» <color:#d9a1a0>%message%"),
                    new PlotChatStyle("halloween", "Halloween", "jack_o_lantern",
                            "<color:#ccc5b9>☠<color:#fffcf2>%plot_id%<color:#ccc5b9>☠ <color:#eb5e28>%username% <color:#eca72c>⚡ <color:#eb5e28>%message%"),
                    new PlotChatStyle("christmas", "Christmas", "spruce_sapling",
                            "<color:#6a994e>⛄<color:#a7c957>%plot_id%<color:#6a994e>⛄ <color:#f2e8cf>%username% <color:#bc4749>★ <color:#f2e8cf>%message%"),
                    new PlotChatStyle("valentines_day", "Valentine's Day", "bow",
                            "<color:#ed4545>❤<color:#fbc6cc>%plot_id%<color:#ed4545>❤ <color:#cab5c8>%username% <color:#ed4545>\uD83C\uDFF9 <color:#cab5c8>%message%"),
                    new PlotChatStyle("orange_tulip", "Orange tulip", "orange_tulip",
                            "<color:#264653>[<color:#2a9d8f>%plot_id%<color:#264653>] <color:#e9c46a>%username% <color:#f4a261>❁ <color:#e76f51>%message%"),
                    new PlotChatStyle("fire", "Fire", "netherrack",
                            "<color:#f1e8b8>[<color:#f9e784>%plot_id%<color:#f1e8b8>] <color:#e58f65>%username% <color:#d05353>\uD83D\uDD25 <color:#e58f65>%message%")
            },
            new CustomColors(
                    "2C8395",
                    new Color[]{
                            new Color("p3_normal", "61C2A2"),
                            new Color("p3_icon", "CCE0D2"),
                            new Color("p3_error", "1D617A"),
                            new Color("p3_red", "A06767"),
                            new Color("p3_green", "67A067"),
                            new Color("p3_blue", "6767A0")
                    }
            ),
            new ReportReason[]{
                    new ReportReason("griffed", 0, new ItemConfig("lava_bucket", 1, false, false, "")),
                    new ReportReason("explicit_sexual_content", 0, new ItemConfig("barrier", 18, false, false, "")),
                    new ReportReason("ip_spam", 0, new ItemConfig("oak_sign", 1, false, false, "")),
                    new ReportReason("generates_lag", 0, new ItemConfig("redstone", 1, false, false, "")),
                    new ReportReason("hate_speech", 0, new ItemConfig("player_head", 1, false, false, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwMThhNDNjYzQ0OGRmZGU5ZWU0Mjg4ZDZjZTQyMWM5NmU4MTQyZmY4YzE5NWM0NDRlMGUxMTg0ZWNmY2M1NSJ9fX0=")),
                    new ReportReason("spawn_kill", 0, new ItemConfig("iron_sword", 1, true, false, "")),
                    new ReportReason("other", 0, new ItemConfig("paper", 1, false, false, ""))
            }
    );

    public record General(boolean autoTeleport, boolean warningUseOfPlotChatInEmptyPlot,
                          boolean logPlotChat, BlockPos defaultPlotSpawn, String[] entityWhitelist,
                          String[] entityRoadWhitelist) {
    }

    public record Database(String type, String user, String password, String database) {
    }

    public static final class CustomColors {
        private String highlight;
        private final Color[] others;

        public CustomColors(String highlight, Color[] others) {
            this.highlight = highlight;
            this.others = others;
        }

        public void setDefaultHighlight() {
            this.highlight = "2C8395";
        }

        public String highlight() {
            return highlight;
        }

        public Color[] others() {
            return others;
        }
    }

    public record Color(String name, String color) {
    }

    public record PlotworldConfig(int roadSize, int plotSize, int minHeight, int maxHeight, String biomeId,
                                  BlockConfig unclaimedBorderBlock, BlockConfig claimedBorderBlock,
                                  BlockConfig borderBlock, BlockConfig roadBlock, Layer[] layers) {

    }

    public record BlockConfig(String id, BlockStateConfig[] states) {

    }

    public record BlockStateConfig(String key, String value) {

    }

    public record Layer(int thickness, BlockConfig block) {

    }

    public record BlockPos(int x, int y, int z) {

    }

    public record ItemConfig(String itemId, int count, boolean hideAttributes, boolean glow, String headValue) {

    }
}


