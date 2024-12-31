package com.github.certifiedtater.lifesteal.gamerules;

import mc.recraftors.unruled_api.impl.GameruleValidatorAdapter;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class BlockGameRuleValidator extends GameruleValidatorAdapter<String> {

    @Override
    public Optional<String> adapt(String s) { // TODO I'm not sure if adaptation even is possible
        return Optional.empty();
    }

    @Override
    public boolean validate(String s) {
        return Registries.BLOCK.containsId(Identifier.tryParse(s));
    }
}
