package com.github.certifiedtater.lifesteal.utils;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class LifestealMixinConfig implements IMixinConfigPlugin {
    private final boolean isInGameTest = FabricLoaderImpl.INSTANCE.getGameDir().toAbsolutePath().toString().contains("gametestServer");
    public static final Logger TEST_LOGGER = LoggerFactory.getLogger("Lifesteal Test");

    @Override
    public void onLoad(String s) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("GameTestUserCacheMixin")) {
            if (isInGameTest) {
                TEST_LOGGER.warn("The UserCache has been replaced with an alternative. Errors may occur");
            }
            return isInGameTest;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
    }
}
