package uk.co.notnull.claimblockvouchers;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemLore;
import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import uk.co.notnull.claimblockvouchers.denominations.CustomDenomination;
import uk.co.notnull.claimblockvouchers.denominations.VoucherDenomination;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class VoucherManager {
    private final ClaimBlockVouchers plugin;
    private final NamespacedKey voucherKey;
    private final HashMap<Integer, VoucherDenomination> denominations = new HashMap<>();
    private CustomDenomination customDenomination;

    public VoucherManager(ClaimBlockVouchers plugin) {
        this.plugin = plugin;
        this.voucherKey = new NamespacedKey(plugin, "voucher");
    }

    public ItemStack createVoucher(VoucherDenomination denomination) {
        ItemStack item = ItemStack.of(Material.FEATHER);

        item.setData(DataComponentTypes.ITEM_NAME, denomination.getItemName());
        item.setData(DataComponentTypes.RARITY, denomination.getRarity());
        item.setData(DataComponentTypes.LORE, ItemLore.lore(denomination.getLore()));

        if (denomination.getItemModel() != null) {
            item.setData(DataComponentTypes.ITEM_MODEL, denomination.getItemModel());
        }

        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
            .addString(String.valueOf(denomination.getBlockCount())));
        item.editPersistentDataContainer(
            pdc -> pdc.set(voucherKey, PersistentDataType.INTEGER,
                denomination.getBlockCount()));

        return item;
    }

    public boolean createVoucherFromBalance(VoucherDenomination denomination, Player player) {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        if (playerData.getRemainingClaimBlocks() < denomination.getBlockCount()) {
            return false;
        }

        ItemStack item = createVoucher(denomination);

        playerData.setBonusClaimBlocks(
            playerData.getBonusClaimBlocks() - denomination.getBlockCount());

        player.getInventory().addItem(item).values()
            .forEach(leftover -> player.getLocation().getWorld()
                .dropItemNaturally(player.getLocation(), leftover));

        GriefPrevention.AddLogEntry(
            "Player " + player.getName() + " created a voucher for " + denomination.getBlockCount()
                + " claim blocks from their balance", CustomLogEntryTypes.Debug);

        return true;
    }

    public boolean isVoucher(@Nullable ItemStack item) {
        return item != null && item.getPersistentDataContainer().has(voucherKey);
    }

    public @Nullable VoucherDenomination getVoucherDenomination(ItemStack item) {
        if (!isVoucher(item)) {
            return null;
        }

        Integer blockCount = item.getPersistentDataContainer()
            .get(voucherKey, PersistentDataType.INTEGER);

        if (blockCount == null) {
            return null;
        }

        return denominations.getOrDefault(blockCount, customDenomination.customDenomination(blockCount));
    }

    public void redeemVoucher(Player player, ItemStack voucher) {
        VoucherDenomination denomination = getVoucherDenomination(voucher);

        if (denomination == null) {
            throw new IllegalArgumentException("Item is not a voucher");
        }

        //add blocks
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        playerData.setBonusClaimBlocks(
            playerData.getBonusClaimBlocks() + denomination.getBlockCount());
        GriefPrevention.instance.dataStore.savePlayerData(player.getUniqueId(), playerData);

        voucher.subtract();

        GriefPrevention.AddLogEntry(
            "Player " + player.getName() + " redeemed a voucher for " + denomination.getBlockCount()
                + " claim blocks", CustomLogEntryTypes.Debug);
    }

    void setDenominations(CustomDenomination customDenomination, Map<Integer, VoucherDenomination> configured) {
        denominations.clear();
        denominations.putAll(configured);
        this.customDenomination = customDenomination;
    }

    CustomDenomination getCustomDenomination() {
        return customDenomination;
    }

    Map<Integer, VoucherDenomination> getConfiguredDenominations() {
        return Collections.unmodifiableMap(denominations);
    }
}
