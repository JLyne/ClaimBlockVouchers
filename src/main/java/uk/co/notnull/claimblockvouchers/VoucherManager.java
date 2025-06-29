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
import uk.co.notnull.messageshelper.Message;

@SuppressWarnings("UnstableApiUsage")
public class VoucherManager {
    private final ClaimBlockVouchers plugin;
    private final NamespacedKey voucherKey;

    public VoucherManager(ClaimBlockVouchers plugin) {
        this.plugin = plugin;
        this.voucherKey = new NamespacedKey(plugin, "voucher");
    }

    public ItemStack createVoucher(VoucherDenomination denomination) {
        ItemStack item = new ItemStack(Material.FEATHER, 1);

        item.setData(DataComponentTypes.ITEM_NAME, denomination.getItemName());
        item.setData(DataComponentTypes.LORE, ItemLore.lore(denomination.getLore()));
        item.setData(DataComponentTypes.ITEM_MODEL, denomination.getItemModel());
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA,
            CustomModelData.customModelData().addString(denomination.getCustomModelData()));
        item.editPersistentDataContainer(
            pdc -> pdc.set(voucherKey, PersistentDataType.INTEGER, denomination.getBlockCount()));

        return item;
    }

    public boolean isVoucher(@Nullable ItemStack item) {
        return item != null && item.getPersistentDataContainer().has(voucherKey);
    }

    public @Nullable VoucherDenomination getVoucherDenomination(ItemStack item) {
        if (!isVoucher(item)) {
            return null;
        }

        return VoucherDenomination.valueOf(
            item.getPersistentDataContainer().get(voucherKey, PersistentDataType.INTEGER)
        );
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

        //inform player
        plugin.messagesHelper.send(player, Message.builder("messages.voucher-redeemed")
            .replacement("total", String.valueOf(playerData.getRemainingClaimBlocks()))
            .build());

        GriefPrevention.AddLogEntry(
            "Player " + player.getName() + " redeemed a voucher for " + denomination.getBlockCount()
                + " claim blocks", CustomLogEntryTypes.Debug);
    }
}
