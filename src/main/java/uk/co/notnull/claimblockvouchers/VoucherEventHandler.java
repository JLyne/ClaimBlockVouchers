package uk.co.notnull.claimblockvouchers;

import static org.bukkit.event.Event.Result.DENY;
import static org.bukkit.inventory.EquipmentSlot.HAND;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import uk.co.notnull.messageshelper.Message;

public class VoucherEventHandler implements Listener {
    private final ClaimBlockVouchers plugin;

    public VoucherEventHandler(ClaimBlockVouchers plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemUse(PlayerInteractEvent event) {
        if (event.useItemInHand() == DENY || !event.getAction().isRightClick()
            || event.getHand() != HAND) {
            return;
        }

        if (plugin.getVoucherManager().isVoucher(event.getItem())) {
            plugin.getVoucherManager().redeemVoucher(event.getPlayer(), event.getItem());

            PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(
                event.getPlayer().getUniqueId());

            //inform player
            plugin.messagesHelper.send(event.getPlayer(), Message.builder("messages.voucher-redeemed")
                .replacement("total", String.valueOf(playerData.getRemainingClaimBlocks()))
                .build());
        }
    }
}
