package uk.co.notnull.claimblockvouchers;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import uk.co.notnull.messageshelper.MessagesHelper;

import java.io.File;

@SuppressWarnings("UnstableApiUsage")
public final class ClaimBlockVouchers extends JavaPlugin implements Listener {
	private static ClaimBlockVouchers instance;
	private VoucherManager voucherManager;
	MessagesHelper messagesHelper = MessagesHelper.getInstance(this);
	private CustomItemsHandler customItemsHandler;

	@Override
	public void onEnable() {
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
	}

	@Override
	public void onDisable() {
		if (customItemsHandler != null) {
			getLogger().info("Disabling CustomItems provider");
			customItemsHandler.unregisterProvider();
			customItemsHandler = null;
		}
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().getName().equals("CustomItems")) {
			getLogger().info("Registering CustomItems provider");
			customItemsHandler = new CustomItemsHandler(this);
		}
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().getName().equals("CustomItems")) {
			if (customItemsHandler != null) {
				getLogger().info("Disabling CustomItems provider");
				customItemsHandler.unregisterProvider();
				customItemsHandler = null;
			}
		}
	}

	public VoucherManager getVoucherManager() {
		return voucherManager;
	}

	public static ClaimBlockVouchers getInstance() {
		return instance;
	}
}
