package uk.co.notnull.claimblockvouchers;

import org.bukkit.Bukkit;
import uk.co.notnull.CustomItems.api.CustomItems;

public final class CustomItemsHandler {;
	private final VoucherItemProvider provider;
	private final CustomItems customItems = (CustomItems) Bukkit.getPluginManager().getPlugin("CustomItems");

	public CustomItemsHandler(ClaimBlockVouchers plugin) {
		provider = new VoucherItemProvider(plugin.getVoucherManager());
		assert customItems != null;
		customItems.getItemManager().registerProvider(provider);
	}

	public void unregisterProvider() {
		assert customItems != null;
		customItems.getItemManager().unregisterProvider(provider);
	}
}
