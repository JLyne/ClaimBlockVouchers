package uk.co.notnull.claimblockvouchers.denominations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemRarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoucherDenomination {
	private static final MiniMessage miniMessage = MiniMessage.miniMessage();

	private final int blockCount;
	private final Component itemName;
	private final NamespacedKey itemModel;
	private final ItemRarity rarity;
	private final List<Component> lore;

	public VoucherDenomination(
			int blockCount, @NotNull String itemName, @Nullable NamespacedKey itemModel,
			@NotNull ItemRarity rarity, @NotNull List<String> lore) {
        if (blockCount <= 0) {
            throw new IllegalArgumentException("blockCount must be positive");
        }

		this.blockCount = blockCount;
		this.itemName = miniMessage.deserialize(itemName,
												Placeholder.unparsed("block_count", String.valueOf(blockCount)));
		this.itemModel = itemModel;
		this.rarity = rarity;
		this.lore = lore.stream().map(l ->
											  miniMessage.deserialize(l,
																	  Placeholder.unparsed("block_count",
																						   String.valueOf(blockCount))))
				.toList();
	}

	public int getBlockCount() {
		return blockCount;
	}

	public @NotNull ItemRarity getRarity() {
		return rarity;
	}

	public @Nullable NamespacedKey getItemModel() {
		return itemModel;
	}

	public @NotNull Component getItemName() {
		return itemName;
	}

	public @NotNull List<Component> getLore() {
		return lore;
	}
}
