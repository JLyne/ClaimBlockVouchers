package uk.co.notnull.claimblockvouchers;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import uk.co.notnull.claimblockvouchers.denominations.VoucherDenomination;
import uk.co.notnull.messageshelper.Message;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class Commands {
	private final ClaimBlockVouchers plugin;

	public Commands(io.papermc.paper.command.brigadier.Commands commands, ClaimBlockVouchers plugin) {
		this.plugin = plugin;

		VoucherDenominationArgumentType voucherDenominationArgumentType =
				new VoucherDenominationArgumentType(plugin.getVoucherManager());

		LiteralCommandNode<CommandSourceStack> giveCommand = literal("givevoucher")
				.requires(source -> source.getSender().hasPermission("claimblockvouchers.give"))
				.then(argument("player", ArgumentTypes.players())
							  .then(argument("voucher", voucherDenominationArgumentType)
									  .executes(ctx -> giveVoucher(ctx, 1))
											.then(argument("amount", IntegerArgumentType.integer(1))
														  .executes(ctx -> giveVoucher(
																  ctx, ctx.getArgument("amount", Integer.class)
														  ))))).build();

		LiteralCommandNode<CommandSourceStack> reloadCommand = literal("cbvreload")
				.requires(source -> source.getSender().hasPermission("claimblockvouchers.reload"))
				.executes(this::reload).build();

		commands.register(giveCommand, "Give claim block vouchers to players");
		commands.register(reloadCommand, "Reload claim block vouchers config");
	}

	private int giveVoucher(CommandContext<CommandSourceStack> ctx, int quantity) throws CommandSyntaxException {
		PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
		VoucherDenomination denomination = ctx.getArgument("voucher", VoucherDenomination.class);
		List<Player> players = resolver.resolve(ctx.getSource());
		ItemStack item = plugin.getVoucherManager().createVoucher(denomination);

		if (quantity > item.getMaxStackSize() * 100) {
			ctx.getSource().getSender().sendMessage(
					Component.translatable("commands.give.failed.toomanyitems")
							.arguments(Component.text(item.getMaxStackSize() * 100), item.displayName()));

			return 0;
		}

		for (Player player : players) {
			int remaining = quantity;

			while (remaining > 0) {
				item.setAmount(Math.min(item.getMaxStackSize(), quantity));
				remaining -= item.getAmount();

				player.getInventory().addItem(item).values()
						.forEach(leftover -> player.getLocation().getWorld()
								.dropItemNaturally(player.getLocation(), leftover));
			}
		}

		Message.Builder builder;

		if (players.size() > 1) {
			builder = Message.builder("messages.voucher-given-multiple")
					.replacement("player_count", String.valueOf(players.size()));
		} else {
			builder = Message.builder("messages.voucher-given-single")
					.replacement("player", players.getFirst().displayName());
		}

		plugin.messagesHelper.send(ctx.getSource().getSender(), builder
				.replacement("item", item.displayName())
				.replacement("quantity", String.valueOf(quantity))
				.build());

		return Command.SINGLE_SUCCESS;
	}
	private int reload(CommandContext<CommandSourceStack> ctx) {
		plugin.reload();

		plugin.messagesHelper.send(ctx.getSource().getSender(),
								   Message.builder("messages.reload-success").build());

		return Command.SINGLE_SUCCESS;
	}
}
