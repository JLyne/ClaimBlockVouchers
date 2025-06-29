package uk.co.notnull.claimblockvouchers;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import uk.co.notnull.CustomItems.api.items.AbstractCustomItem;
import uk.co.notnull.CustomItems.api.items.CreationContext;
import uk.co.notnull.claimblockvouchers.denominations.VoucherDenomination;

public class VoucherCustomItem extends AbstractCustomItem {
    private final VoucherDenomination denomination;

    public VoucherCustomItem(VoucherDenomination denomination) {
        super(new NamespacedKey(ClaimBlockVouchers.getInstance(),
                "voucher_" + denomination.getBlockCount()),
            denomination.getItemName(), false);
        this.denomination = denomination;
    }

    @Override
    public ItemStack createItem(CreationContext creationContext, int i) {
        return ClaimBlockVouchers.getInstance().getVoucherManager().createVoucher(denomination);
    }
}
