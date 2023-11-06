package gripe._90.megacells.integration.appbot;

import static gripe._90.megacells.definition.MEGAItems.GREATER_ENERGY_CARD;

import appeng.api.upgrades.Upgrades;
import appeng.core.localization.GuiText;

import gripe._90.megacells.core.Platform;

public final class AppBotIntegration {
    public static void initUpgrades(Platform platform) {
        AppBotItems.getPortables()
                .forEach(c -> Upgrades.add(GREATER_ENERGY_CARD, c, 2, GuiText.PortableCells.getTranslationKey()));

        for (var portable : platform.getAppBotPortableCells()) {
            Upgrades.add(GREATER_ENERGY_CARD, portable, 2, GuiText.PortableCells.getTranslationKey());
        }
    }
}
