package uk.co.notnull.claimblockvouchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public enum VoucherDenomination {
    ONE(1),
    FIFTY(50),
    ONE_HUNDRED(100),
    ONE_THOUSAND(1000);

    private static final Map<Integer, VoucherDenomination> map = new HashMap<>();

    static {
        for (VoucherDenomination tier : VoucherDenomination.values()) {
            map.put(tier.blockCount, tier);
        }
    }

    private final int blockCount;
    private final NamespacedKey itemModel;
    private final String customModelData;
    private final TranslatableComponent itemName;
    private final List<TranslatableComponent> lore;

    VoucherDenomination(int blockCount) {
        this.blockCount = blockCount;
        this.itemModel = new NamespacedKey("rtgame", "claim_block_voucher");
        this.itemName = Component.translatable(
                String.format("item.claimblockvoucher.%s.name", blockCount),
                String.format("Claim Block Voucher - %d blocks", blockCount));
        this.lore = List.of(
            Component.translatable("item.claimblockvoucher." + blockCount + ".lore.line1")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GRAY),
            Component.translatable("item.claimblockvoucher." + blockCount + ".lore.line2")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GRAY));
        this.customModelData = String.valueOf(blockCount);
    }

    public static @Nullable VoucherDenomination valueOf(int tier) {
        return map.get(tier);
    }

    public int getBlockCount() {
        return blockCount;
    }

    public NamespacedKey getItemModel() {
        return itemModel;
    }

    public String getCustomModelData() {
        return customModelData;
    }

    public TranslatableComponent getItemName() {
        return itemName;
    }

    public List<TranslatableComponent> getLore() {
        return lore;
    }
}
