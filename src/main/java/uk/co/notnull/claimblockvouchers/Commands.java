package uk.co.notnull.claimblockvouchers;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

        LiteralCommandNode<CommandSourceStack> createCommand = literal("createvoucher")
				.requires(source -> source.getSender().hasPermission("claimblockvouchers.create"))
                .executes(this::openDialog)
				.then(argument("voucher", voucherDenominationArgumentType)
                    .executes(this::createVoucher)).build();

        LiteralCommandNode<CommandSourceStack> openCommand = literal("opencreatedialog")
				.requires(source -> source.getSender().hasPermission("claimblockvouchers.opendialog"))
				.then(argument("player", ArgumentTypes.players())
                    .executes(this::openDialogForPlayer))
        .build();

		LiteralCommandNode<CommandSourceStack> reloadCommand = literal("cbvreload")
				.requires(source -> source.getSender().hasPermission("claimblockvouchers.reload"))
				.executes(this::reload).build();

		commands.register(giveCommand, "Give claim block vouchers to players");
		commands.register(createCommand, "Create a claim block voucher from your available claim blocks");
		commands.register(openCommand, "Opens the create claim block voucher dialog for a player");
		commands.register(reloadCommand, "Reload claim block vouchers config");
	}

	private int giveVoucher(CommandContext<CommandSourceStack> ctx, int quantity) throws CommandSyntaxException {
		PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
		VoucherDenomination denomination = ctx.getArgument("voucher", VoucherDenomination.class);
		List<Player> players = resolver.resolve(ctx.getSource());
		ItemStack item = plugin.getVoucherManager().createVoucher(denomination);

        if (players.isEmpty()) {
            ctx.getSource().getSender()
                .sendMessage(Component.translatable("argument.entity.notfound.player")
                    .color(NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

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

            plugin.messagesHelper.send(ctx.getSource().getSender(),
                Message.builder("messages.voucher-given")
                    .replacement("player", players.getFirst().displayName())
                    .replacement("item", item.displayName())
				    .replacement("quantity", String.valueOf(quantity))
                    .build());
		}

		return Command.SINGLE_SUCCESS;
	}

    private int openDialogForPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
		List<Player> players = resolver.resolve(ctx.getSource());

        if (players.isEmpty()) {
            ctx.getSource().getSender()
                .sendMessage(Component.translatable("argument.entity.notfound.player")
                    .color(NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

		for (Player player : players) {
			plugin.getCreateVoucherDialog().open(player);
            plugin.messagesHelper.send(ctx.getSource().getSender(),
                Message.builder("messages.dialog-opened")
                    .replacement("player", players.getFirst().displayName())
                    .build());
		}

        return Command.SINGLE_SUCCESS;
	}

    private int openDialog(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return Command.SINGLE_SUCCESS;
        }

        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        if (playerData.getRemainingClaimBlocks() == 0) {
            plugin.messagesHelper.send(ctx.getSource().getSender(),
                Message.builder("messages.insufficient-claim-blocks").build());

            return Command.SINGLE_SUCCESS;
        }

        plugin.getCreateVoucherDialog().open(player);

		return Command.SINGLE_SUCCESS;
	}

    private int createVoucher(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return Command.SINGLE_SUCCESS;
        }

		VoucherDenomination denomination = ctx.getArgument("voucher", VoucherDenomination.class);

        if (!plugin.getVoucherManager().createVoucherFromBalance(denomination, player)) {
            plugin.messagesHelper.send(ctx.getSource().getSender(),
                Message.builder("messages.insufficient-claim-blocks").build());

		    return Command.SINGLE_SUCCESS;
        }

        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

		plugin.messagesHelper.send(ctx.getSource().getSender(),
            Message.builder("messages.voucher-created")
                .replacement("blocks", String.valueOf(denomination.getBlockCount()))
                .replacement("total", String.valueOf(playerData.getRemainingClaimBlocks()))
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
