package com.github.certifiedtater.lifesteal.gamerules;

import net.fabricmc.fabric.mixin.gamerule.GameRulesIntRuleAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncedBoundedIntRule extends GameRules.IntRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncedBoundedIntRule.class);
    private final int minimumValue;
    private final int maximumValue;

    public SyncedBoundedIntRule(GameRules.Type<GameRules.IntRule> type, int initialValue, int minimumValue, int maximumValue) {
        super(type, initialValue);
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    protected void deserialize(String value) {
        int i = parseInt(value);
        if (this.minimumValue <= i && this.maximumValue >= i) {
            ((GameRulesIntRuleAccessor)this).setValue(i);
        } else {
            LOGGER.warn("Failed to parse integer {}. Was out of bounds {} - {}", value, this.minimumValue, this.maximumValue);
        }
    }

    public boolean validateAndSet(String input) {
        try {
            int value = Integer.parseInt(input);
            MinecraftServer server = LifeStealGamerules.serverInstance;
            if (server != null) {
                int killValue = LifeStealGamerules.serverInstance.getGameRules().getInt(LifeStealGamerules.STEALAMOUNT);

                if (killValue % 2 == 0) { // If killValue is even
                    if (value % 2 != 0) { // value should also be even
                        return false;
                    } // If killValue is odd, then value can be anything
                }
            }

            if (this.minimumValue <= value && this.maximumValue >= value) {
                ((GameRulesIntRuleAccessor)this).setValue(value);
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException var31) {
            return false;
        }
    }

    protected GameRules.IntRule copy() {
        return new SyncedBoundedIntRule(this.type, ((GameRulesIntRuleAccessor)this).getValue(), this.minimumValue, this.maximumValue);
    }

    private static int parseInt(String input) {
        if (!input.isEmpty()) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException var2) {
                LOGGER.warn("Failed to parse integer {}", input);
            }
        }

        return 0;
    }
}
