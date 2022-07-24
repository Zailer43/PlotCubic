package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ReportReason implements IBooleanOption {
    public static final HashMap<String, ReportReason> REPORT_REASON_HASH_MAP = new HashMap<>();
    public static final ReportReason GRIFFED = new ReportReason("griffed", Items.LAVA_BUCKET);
    public static final ReportReason EXPLICIT_SEXUAL_CONTENT = new ReportReason("explicit_sexual_content", Items.BARRIER, 18);
    public static final ReportReason IP_SPAM = new ReportReason("ip_spam", Items.OAK_SIGN);
    public static final ReportReason GENERATES_LAG = new ReportReason("generates_lag", Items.REDSTONE);
    public static final ReportReason HATE_SPEECH = new ReportReason("hate_speech", Items.PLAYER_HEAD);
    public static final ReportReason SPAWN_KILL = new ReportReason("spawn_kill", Items.IRON_SWORD, true);
    public static final ReportReason OTHER = new ReportReason("other", Items.MINECART, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwMThhNDNjYzQ0OGRmZGU5ZWU0Mjg4ZDZjZTQyMWM5NmU4MTQyZmY4YzE5NWM0NDRlMGUxMTg0ZWNmY2M1NSJ9fX0=");

    private static final String BASE_TRANSLATION_KEY = "text.plotcubic.report_reason.";
    private final String id;
    private final Item item;
    private final int count;
    private final boolean hideAttributes;
    private final String headValue;

    public ReportReason(String id, Item item) {
        this(id, item, false);
    }

    public ReportReason(String id, Item item, boolean hideAttributes) {
        this(id, item, 1, hideAttributes, null);
    }

    public ReportReason(String id, Item item, int count) {
        this(id, item, count, false, null);
    }

    public ReportReason(String id, Item item, String headValue) {
        this(id, item, 1, false, headValue);
    }

    public ReportReason(String id, Item item, int count, boolean hideAttributes, @Nullable String headValue) {
        this.id = id;
        this.item = item;
        this.count = count;
        this.hideAttributes = hideAttributes;
        this.headValue = headValue;
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

    public static void register() {
        register(EXPLICIT_SEXUAL_CONTENT);
        register(GRIFFED);
        register(GENERATES_LAG);
        register(HATE_SPEECH);
        register(IP_SPAM);
        register(SPAWN_KILL);
        register(OTHER);
    }

    public static void register(ReportReason reportReason) {
        REPORT_REASON_HASH_MAP.put(reportReason.id, reportReason);
    }
}
