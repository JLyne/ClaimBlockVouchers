package uk.co.notnull.claimblockvouchers.denominations;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemRarity;
import java.util.List;

public class CustomDenomination {
    private final String itemName;
    private final NamespacedKey itemModel;
    private final ItemRarity rarity;
    private final List<String> lore;

    public CustomDenomination(String itemName, NamespacedKey itemModel, ItemRarity rarity, List<String> lore) {
        this.itemName = itemName;
        this.itemModel = itemModel;
        this.rarity = rarity;
        this.lore = lore;
    }

    public VoucherDenomination customDenomination(int blockCount) {
        return new VoucherDenomination(blockCount, itemName, itemModel, rarity, lore);
    }
}
