package me.zailer.plotcubic.config;

import me.zailer.plotcubic.plot.PlotChatStyle;

public record Config(Database database, PlotChatStyle[] plotChatStyles) {

    public static final Config DEFAULT = new Config(
            new Database(
                    "mysql",
                    "localhost",
                    3306,
                    "root",
                    "123",
                    "plotcubic",
                    "p3"),
            new PlotChatStyle[]{
                    new PlotChatStyle("frozen_berries", "Frozen Berries", "sweet_berries", "[", "]", 0x595E60, 0x707370,
                            0xB0B7C0, "»", 0x707370, 0xB0B7C0),
                    new PlotChatStyle("window_tile", "Window Tide", "cyan_stained_glass_pane", "[", "]", 0x41729F, 0x5885AF,
                            0xC3E0E5, "»", 0x5885AF, 0xC3E0E5),
                    new PlotChatStyle("rosy_flamingo", "Rosy Flamingo", "pink_dye", "[", "]", 0x67595E, 0xA49393,
                            0xE8B4B8, "»", 0xEED6D3, 0xE8B4B8),
                    new PlotChatStyle("healthy_leaves", "Healthy Leaves", "azalea_leaves", "[", "]", 0x3D550C, 0x81B622,
                            0xECF87F, "»", 0x81B622, 0xECF87F),
                    new PlotChatStyle("candy_hearts", "Candy Hearts", "cake", "[", "]", 0xFCECA5, 0xFEC8A7,
                            0xB5EECB, "»", 0xCBC7FC, 0xB5EECB),
                    new PlotChatStyle("sweeter_love", "Sweeter Love", "red_dye", "[", "]", 0x838BC2, 0xA58CB3,
                            0xD9A1A0, "»", 0xEAE7FA, 0xD9A1A0),
                    new PlotChatStyle("halloween", "Halloween", "jack_o_lantern", "☠", "☠", 0xCCC5B9, 0xFFFCF2,
                            0xEB5E28, "⚡", 0xECA72C, 0xEB5E28),
                    new PlotChatStyle("christmas", "Christmas", "spruce_sapling", "⛄", "⛄", 0x6A994E, 0xA7C957,
                            0xF2E8CF, "★", 0xBC4749, 0xF2E8CF),
                    new PlotChatStyle("valentines_day", "Valentine's Day", "bow", "❤", "❤", 0xED4545, 0xFBC6CC,
                            0xCAB5C8, "\uD83C\uDFF9", 0xED4545, 0xCAB5C8)
            }
    );

    public record Database(String type, String host, Integer port, String user, String password,
                           String database, String table_name) {
    }
}


