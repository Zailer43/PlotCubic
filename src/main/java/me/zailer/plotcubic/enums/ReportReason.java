package me.zailer.plotcubic.enums;

import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import javax.annotation.Nullable;

public enum ReportReason implements IBooleanOption {
    GRIFFED("Griffed", Items.LAVA_BUCKET),
    EXPLICIT_SEXUAL_CONTENT("Explicit sexual content", Items.BARRIER, 18),
    IP_SPAM("IP spam", Items.OAK_SIGN),
    GENERATES_LAG("Generates lag", Items.REDSTONE),
    HATE_SPEECH("Hate speech", Items.PLAYER_HEAD),
    SPAWN_KILL("Spawn kill", Items.IRON_SWORD, true),
    OTHER("Other", Items.MINECART, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwMThhNDNjYzQ0OGRmZGU5ZWU0Mjg4ZDZjZTQyMWM5NmU4MTQyZmY4YzE5NWM0NDRlMGUxMTg0ZWNmY2M1NSJ9fX0=");

    private final String name;
    private final Item item;
    private final int count;
    private final boolean hideAttributes;
    private final String headValue;

    ReportReason(String name, Item item) {
        this(name, item, false);
    }

    ReportReason(String name, Item item, boolean hideAttributes) {
        this(name, item, 1, hideAttributes, null);
    }

    ReportReason(String name, Item item, int count) {
        this(name, item, count, false, null);
    }

    ReportReason(String name, Item item, String headValue) {
        this(name, item, 1, false, headValue);
    }

    ReportReason(String name, Item item, int count, boolean hideAttributes, @Nullable String headValue) {
        this.name = name;
        this.item = item;
        this.count = count;
        this.hideAttributes = hideAttributes;
        this.headValue = headValue;
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
        return this.count;
    }

    @Override
    public boolean isHideAttributes() {
        return this.hideAttributes;
    }

    @Override
    public boolean hasHeadValue() {
        return this.headValue != null;
    }

    @Override
    @Nullable
    public String getHeadValue() {
        return this.headValue;
    }
}
