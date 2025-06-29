package uk.co.notnull.claimblockvouchers;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import uk.co.notnull.claimblockvouchers.denominations.CustomDenomination;
import uk.co.notnull.claimblockvouchers.denominations.VoucherDenomination;
import uk.co.notnull.messageshelper.MessagesHelper;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class ClaimBlockVouchers extends JavaPlugin implements Listener {
	private static ClaimBlockVouchers instance;

	private VoucherManager voucherManager;
	MessagesHelper messagesHelper = MessagesHelper.getInstance(this);
	private CustomItemsHandler customItemsHandler;

	@Override
	public void onEnable() {
		initConfig();
		instance = this;
		voucherManager = new VoucherManager(this);

		File messages = new File(getDataFolder(), "messages.yml");
        if(!messages.exists()) {
            saveResource("messages.yml", false);
        }

		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new VoucherEventHandler(this), this);
        messagesHelper.loadMessages(messages);

		LifecycleEventManager<@NotNull Plugin> manager = getLifecycleManager();
		manager.registerEventHandler(LifecycleEvents.COMMANDS,
									 event -> new Commands(event.registrar(), this));
		reload();
	}

	@Override
	public void onDisable() {
		disableCustomItems();
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().getName().equals("CustomItems")) {
			enableCustomItems();
		}
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().getName().equals("CustomItems")) {
			disableCustomItems();
		}
	}

	private void initConfig() {
		getConfig().options().copyDefaults(true);
		getConfig().options().parseComments(true);

		getConfig().addDefault("default.item-name", "Claim Block Voucher - <block_count> Blocks");
		getConfig().addDefault("default.item-model", "minecraft:feather");
		getConfig().addDefault("default.lore", Collections.emptyList());
		getConfig().addDefault("default.rarity", ItemRarity.COMMON.name());

		getConfig().addDefault("denominations.1.block-count", 1);
		getConfig().addDefault("denominations.1.item-name", "Claim Block Voucher - Single Block");
		getConfig().addDefault("denominations.100.block-count", 100);
		getConfig().addDefault("denominations.100.rarity", ItemRarity.UNCOMMON.name());
		getConfig().addDefault("denominations.1000.block-count", 1000);
		getConfig().addDefault("denominations.1000.rarity", ItemRarity.RARE.name());

		saveConfig();
		reloadConfig();

		getConfig().setComments("default", List.of("Data applied to all voucher items unless overridden in a denomination", "Vouchers with amounts will always use these values"));
		getConfig().setComments("denomination", List.of("Overrides for vouchers valued at specific numbers of claim blocks"));
		saveConfig();
	}

	void reload() {
		reloadConfig();
		initDenominations();
		disableCustomItems();
		enableCustomItems();
	}

	private void initDenominations() {
		HashMap<Integer, VoucherDenomination> denominations = new HashMap<>();

		String defaultItemName = getConfig().getString("default.item-name", "Claim Block Voucher");
		NamespacedKey defaultItemModel = NamespacedKey.fromString(
				getConfig().getString("default.item-model", ""));
		ItemRarity rarity = ItemRarity.COMMON;

		try {
			rarity = ItemRarity.valueOf(
					getConfig().getString("default.rarity", ItemRarity.COMMON.name()));
		} catch (IllegalArgumentException e) {
			getLogger().warning("Invalid default rarity");
		}

		ItemRarity defaultRarity = rarity;
		List<String> defaultLore = getConfig().getStringList("default.lore");
		CustomDenomination customDenomination = new CustomDenomination(defaultItemName, defaultItemModel,
																	   defaultRarity, defaultLore);

		if (getConfig().contains("denominations")) {
			getConfig().getConfigurationSection("denominations").getValues(false).entrySet().forEach(entry -> {
				if (!(entry.getValue() instanceof ConfigurationSection section)) {
					getLogger().warning("Invalid config for denomination " + entry.getKey());
					return;
				}

				int blockCount = section.getInt("block-count");

				if (blockCount == 0) {
					getLogger().warning("Invalid block-count for denomination " + entry.getKey());
					return;
				}

				String itemName = section.getString("item-name", defaultItemName);

				NamespacedKey itemModel = defaultItemModel;

				if (section.contains("item-model")) {
					itemModel = NamespacedKey.fromString(section.getString("item-name", ""));
				}

				ItemRarity denominationRarity = defaultRarity;

				if (section.contains("rarity")) {
					try {
						denominationRarity = ItemRarity.valueOf(
								section.getString("rarity", ItemRarity.COMMON.name()));
					} catch (IllegalArgumentException e) {
						getLogger().warning("Invalid rarity for denomination " + entry.getKey());
					}
				}

				List<String> lore = defaultLore;

				if (section.contains("lore")) {
					lore = section.getStringList("lore");
				}

				denominations.put(blockCount, new VoucherDenomination(blockCount, itemName, itemModel,
																	  denominationRarity, lore));
			});
		}

		voucherManager.setDenominations(customDenomination, denominations);
	}

	private void enableCustomItems() {
		if (customItemsHandler == null && getServer().getPluginManager().isPluginEnabled("CustomItems")) {
			getLogger().info("Registering CustomItems provider");
			customItemsHandler = new CustomItemsHandler(this);
		}
	}

	private void disableCustomItems() {
		if (customItemsHandler != null) {
			getLogger().info("Disabling CustomItems provider");
			customItemsHandler.unregisterProvider();
			customItemsHandler = null;
		}
	}

	public VoucherManager getVoucherManager() {
		return voucherManager;
	}

	public static ClaimBlockVouchers getInstance() {
		return instance;
	}
}
