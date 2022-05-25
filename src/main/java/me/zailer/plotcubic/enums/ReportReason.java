package me.zailer.plotcubic.enums;

import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;

public enum ReportReason implements IBooleanOption {
    GRIFFED("griffed", Items.LAVA_BUCKET),
    EXPLICIT_SEXUAL_CONTENT("explicit_sexual_content", Items.BARRIER, 18),
    IP_SPAM("ip_spam", Items.OAK_SIGN),
    GENERATES_LAG("generates_lag", Items.REDSTONE),
    HATE_SPEECH("hate_speech", Items.PLAYER_HEAD),
    SPAWN_KILL("spawn_kill", Items.IRON_SWORD, true),
    OTHER("other", Items.MINECART, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwMThhNDNjYzQ0OGRmZGU5ZWU0Mjg4ZDZjZTQyMWM5NmU4MTQyZmY4YzE5NWM0NDRlMGUxMTg0ZWNmY2M1NSJ9fX0=");

    private static final String BASE_TRANSLATION_KEY = "text.plotcubic.report_reason.";
    private final String translationKey;
    private final Item item;
    private final int count;
    private final boolean hideAttributes;
    private final String headValue;

    ReportReason(String translationKey, Item item) {
        this(translationKey, item, false);
    }

    ReportReason(String translationKey, Item item, boolean hideAttributes) {
        this(translationKey, item, 1, hideAttributes, null);
    }

    ReportReason(String translationKey, Item item, int count) {
        this(translationKey, item, count, false, null);
    }

    ReportReason(String translationKey, Item item, String headValue) {
        this(translationKey, item, 1, false, headValue);
    }

    ReportReason(String translationKey, Item item, int count, boolean hideAttributes, @Nullable String headValue) {
        this.translationKey = translationKey;
        this.item = item;
        this.count = count;
        this.hideAttributes = hideAttributes;
        this.headValue = headValue;
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
