package uk.co.notnull.claimblockvouchers;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.ActionButton.Builder;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogBase.DialogAfterAction;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import uk.co.notnull.claimblockvouchers.denominations.VoucherDenomination;
import uk.co.notnull.messageshelper.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
class CreateVoucherDialog {
    private final ClaimBlockVouchers plugin;

    private final Sound successSound = Sound.sound()
        .type(NamespacedKey.minecraft("entity.experience_orb.pickup"))
        .build();

    private DialogBody voucherItem;

    private Component insufficientBlocksError;
    private Component invalidValueError;
    private Component createSuccess;

    private Component createTitle;
    private PlainMessageDialogBody createIntro;
    private Component customTitle;
    private PlainMessageDialogBody customIntro;
    private Component customInputLabel;

    private final Map<VoucherDenomination, Builder> denominationButtons = new LinkedHashMap<>();
    private Builder createCustomButton;
    private Builder createExitButton;
    private Builder customCreateButton;
    private Builder customExitButton;

    CreateVoucherDialog(ClaimBlockVouchers plugin) {
        this.plugin = plugin;
    }

    void reload() {
        denominationButtons.clear();

        // Body
        voucherItem = DialogBody.item(plugin.getVoucherManager().createVoucher(
            plugin.getVoucherManager().getCustomDenomination().customDenomination(67)))
            .build();

        createTitle = plugin.messagesHelper.getComponent(
            Message.builder("dialog.create.title")
                .build());

        createIntro = DialogBody.plainMessage(plugin.messagesHelper.getComponent(
            Message.builder("dialog.create.intro")
                .build()));

        customTitle = plugin.messagesHelper.getComponent(
            Message.builder("dialog.custom.title")
                .build());

        customIntro = DialogBody.plainMessage(plugin.messagesHelper.getComponent(
            Message.builder("dialog.custom.intro")
                .build()));

        customInputLabel = plugin.messagesHelper.getComponent(
            Message.builder("dialog.custom.input-label")
                .build());

        insufficientBlocksError = plugin.messagesHelper.getComponent(
            Message.builder("dialog.common.insufficient-claim-blocks")
                .build());

        invalidValueError = plugin.messagesHelper.getComponent(
            Message.builder("dialog.custom.invalid-value")
                .build());

        createSuccess = plugin.messagesHelper.getComponent(
            Message.builder("dialog.common.voucher-created")
                .build());

        createExitButton = ActionButton.builder(
            plugin.messagesHelper.getComponent("dialog.create.exit-button-label"));

        customExitButton = ActionButton.builder(
            plugin.messagesHelper.getComponent("dialog.custom.exit-button-label"));

        // Denomination buttons
        plugin.getVoucherManager().getConfiguredDenominations().values().stream()
            .sorted(Comparator.comparingInt(VoucherDenomination::getBlockCount))
            .forEach(denomination -> {
                Component label = plugin.messagesHelper.getComponent(
                    Message.builder("dialog.create.denomination-button-label")
                        .replacement("amount", String.valueOf(denomination.getBlockCount()))
                        .build());

                Component tooltip = plugin.messagesHelper.getComponent(
                    Message.builder("dialog.create.denomination-button-tooltip")
                        .replacement("amount", String.valueOf(denomination.getBlockCount()))
                        .build());

                denominationButtons.put(denomination, ActionButton.builder(label)
                    .tooltip(tooltip).width(100));
            });

        // Custom amount button
        Component customLabel = plugin.messagesHelper.getComponent(
            Message.builder("dialog.create.custom-button-label")
                .build());

        Component customTooltip = plugin.messagesHelper.getComponent(
            Message.builder("dialog.create.custom-button-tooltip")
                .build());

        createCustomButton = ActionButton.builder(customLabel).tooltip(customTooltip).width(100);

        // Create button in custom value dialog
        Component customCreateLabel = plugin.messagesHelper.getComponent(
            Message.builder("dialog.custom.create-label")
                .build());

        Component customCreateTooltip = plugin.messagesHelper.getComponent(
            Message.builder("dialog.custom.create-tooltip")
                .build());

        customCreateButton = ActionButton.builder(customCreateLabel)
            .tooltip(customCreateTooltip).width(100);
    }

    public void open(Player player) {
        open(player, null);
    }

    public void open(Player player, @Nullable Component message) {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        DialogBody blocksAvailable = DialogBody.plainMessage(plugin.messagesHelper.getComponent(
            Message.builder("dialog.common.blocks-available")
                .replacement("total", String.valueOf(playerData.getRemainingClaimBlocks()))
                .build()));

        // Body
        List<DialogBody> body;

        if (message != null) {
            body = List.of(voucherItem, DialogBody.plainMessage(message), blocksAvailable); // Replace intro with message
        } else {
            body = List.of(voucherItem, createIntro, blocksAvailable);
        }

        // Buttons
        List<ActionButton> actions = new ArrayList<>();

        // Create callbacks here as they have limited lifetime
        denominationButtons.entrySet().stream().map(e ->
            e.getValue().action(DialogAction.customClick((view, audience) -> {
                if (audience instanceof Player p) {
                    if (plugin.getVoucherManager().createVoucherFromBalance(e.getKey(), player)) {
                        open(p, createSuccess); // Reopen dialog with success
                        p.playSound(successSound);
                    } else {
                        open(p, insufficientBlocksError); // Reopen dialog with error
                    }
                }
            }, ClickCallback.Options.builder().build())).build()).forEach(actions::add);

        actions.add(createCustomButton.action(DialogAction.customClick((view, audience) -> {
            if (audience instanceof Player p) {
                openCustom(p);
            }
        }, ClickCallback.Options.builder().build())).build());

        Dialog dialog =
            Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(createTitle)
                    .canCloseWithEscape(true)
                    .afterAction(DialogAfterAction.WAIT_FOR_RESPONSE)
                    .externalTitle(createTitle)
                    .body(body)
                    .build())
                .type(DialogType.multiAction(actions)
                    .exitAction(createExitButton.action(
                        // Close waiting for server screen
                        DialogAction.customClick(
                            (_, audience) -> {
                                audience.closeDialog(); // This doesn't seem to work?
                                if (audience instanceof Player p) {
                                    p.closeInventory(); // This does but ew?
                                }
                            },
                            ClickCallback.Options.builder().build()))
                        .build())
                    .build()));

        player.showDialog(dialog);
    }

    public void openCustom(Player player) {
        openCustom(player, null, null);
    }

    public void openCustom(Player player, @Nullable Integer initialAmount, @Nullable Component message) {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        DialogBody blocksAvailable = DialogBody.plainMessage(plugin.messagesHelper.getComponent(
            Message.builder("dialog.common.blocks-available")
                .replacement("total", String.valueOf(playerData.getRemainingClaimBlocks()))
                .build()));

        // Body
        List<DialogBody> body;

        if (message != null) {
            body = List.of(voucherItem, DialogBody.plainMessage(message), blocksAvailable); // Replace intro with message
        } else {
            body = List.of(voucherItem, customIntro, blocksAvailable);
        }

        // Input
        String initialValue = initialAmount != null ?
            String.valueOf(initialAmount) : String.valueOf(playerData.getRemainingClaimBlocks());

        List<DialogInput> inputs = Collections.singletonList(
            DialogInput.text("amount", 150, customInputLabel, true,
                initialValue, 10, null));

        // Create button
        // Create callback here as they have limited lifetime
        List<ActionButton> actions = Collections.singletonList(
            customCreateButton.action(DialogAction.customClick((view, audience) -> {
                if (audience instanceof Player p) {
                    try {
                        // Validate
                        String amountString = view.getText("amount");
                        int amount = amountString != null ? Integer.parseInt(amountString) : 0;

                        if (amount <= 0) {
                            openCustom(p, null, invalidValueError); // Reopen dialog with error
                        } else {
                            VoucherDenomination denomination = plugin.getVoucherManager()
                                .getCustomDenomination().customDenomination(amount);

                            if (plugin.getVoucherManager().createVoucherFromBalance(denomination, player)) {
                                openCustom(p, amount, createSuccess); // Reopen dialog with success
                                p.playSound(successSound);
                            } else {
                                openCustom(p, amount, insufficientBlocksError); // Reopen dialog with error
                            }
                        }
                    } catch (NumberFormatException e) {
                        openCustom(p, null, invalidValueError); // Reopen dialog with error
                    }
                }
            }, ClickCallback.Options.builder().build())).build());

        Dialog dialog =
            Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(customTitle)
                    .canCloseWithEscape(true)
                    .afterAction(DialogAfterAction.WAIT_FOR_RESPONSE)
                    .externalTitle(customTitle)
                    .body(body)
                    .inputs(inputs)
                    .build())
                .type(DialogType.multiAction(actions)
                    .exitAction(customExitButton.action(
                        // Go back to first dialog
                        DialogAction.customClick(
                            (_, audience) -> {
                                if (audience instanceof Player p) {
                                    open(p);
                                }
                            },
                            ClickCallback.Options.builder().build()))
                        .build())
                    .build()));

        player.showDialog(dialog);
    }
}
