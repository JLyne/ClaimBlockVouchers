package uk.co.notnull.claimblockvouchers;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;
import uk.co.notnull.claimblockvouchers.denominations.VoucherDenomination;

/**
 * Argument parser for {@link VoucherDenomination}
 *
 * @since 1.1.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class VoucherDenominationArgumentType implements CustomArgumentType.Converted<VoucherDenomination, Integer> {
    private final VoucherManager manager;

    public VoucherDenominationArgumentType(VoucherManager manager) {
        this.manager = manager;
    }

	@Override
	public @NotNull VoucherDenomination convert(@NotNull Integer input) {
        return manager.getConfiguredDenominations()
            .getOrDefault(input, manager.getCustomDenomination().customDenomination(input));
	}

	@Override
	public @NotNull IntegerArgumentType getNativeType() {
		return IntegerArgumentType.integer(1);
	}

	@Override
	public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(
			com.mojang.brigadier.context.@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
		String search = builder.getRemainingLowerCase();

        manager.getConfiguredDenominations().keySet().stream()
            .filter(key -> key.toString().startsWith(search))
            .forEach(builder::suggest);

		return CompletableFuture.completedFuture(builder.build());
	}
}
