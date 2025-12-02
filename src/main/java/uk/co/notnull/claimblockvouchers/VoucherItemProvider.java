package uk.co.notnull.claimblockvouchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.notnull.CustomItems.api.items.CustomItem;
import uk.co.notnull.CustomItems.api.items.provider.CustomItemProvider;
import uk.co.notnull.claimblockvouchers.denominations.VoucherDenomination;

public class VoucherItemProvider implements CustomItemProvider {
    private final VoucherManager manager;
    private final Map<VoucherDenomination, VoucherCustomItem> items = new HashMap<>();

    public VoucherItemProvider(VoucherManager manager) {
        this.manager = manager;

        for (VoucherDenomination denomination : manager.getConfiguredDenominations().values()) {
            items.put(denomination, new VoucherCustomItem(denomination));
        }
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return ClaimBlockVouchers.getInstance();
    }

    @Override
    public @NotNull List<CustomItem> provideItems() {
        return new ArrayList<>(items.values());
    }

    @Nullable
    @Override
    public CustomItem identifyItem(ItemStack itemStack) {
        return items.get(manager.getVoucherDenomination(itemStack));
    }
}
