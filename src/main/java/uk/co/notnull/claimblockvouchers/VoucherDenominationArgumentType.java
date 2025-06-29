package uk.co.notnull.claimblockvouchers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

/**
 * Argument parser for {@link VoucherDenomination}
 *
 * @since 1.1.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class VoucherDenominationArgumentType implements CustomArgumentType.Converted<VoucherDenomination, String> {
	private final List<String> denominations = Arrays.stream(VoucherDenomination.values()).map(c -> c.name().toLowerCase()).toList();

	@Override
	public @NotNull VoucherDenomination convert(@NotNull String input) throws CommandSyntaxException {
		if(!denominations.contains(input.toLowerCase())) {
			throw new SimpleCommandExceptionType(new LiteralMessage(input + " is not a valid voucher denomination"))
					.create();
		}

		return VoucherDenomination.valueOf(input.toUpperCase());
	}

	@Override
	public @NotNull StringArgumentType getNativeType() {
		return StringArgumentType.word();
	}

	@Override
	public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(
			com.mojang.brigadier.context.@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
		String search = builder.getRemainingLowerCase();

		denominations.stream().filter(
            denomination -> denomination.startsWith(search)).forEach(builder::suggest);

		return CompletableFuture.completedFuture(builder.build());
	}
}
