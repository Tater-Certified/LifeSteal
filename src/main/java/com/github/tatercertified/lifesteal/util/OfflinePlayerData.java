package com.github.tatercertified.lifesteal.util;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Ampflower
 * @since 1.4.0
 **/
public class OfflinePlayerData {
    private static final Logger logger = LogUtils.getLogger();

    public final GameProfile holder;
    public final NbtCompound root;

    private final Path dir;

    protected OfflinePlayerData(GameProfile holder, NbtCompound root, Path dir) {
        this.holder = holder;
        this.root = root;
        this.dir = dir;
    }

    @Nullable
    public LifeStealPlayerData getLifeStealData() {
        if (root.contains("lifeStealData", NbtElement.COMPOUND_TYPE)) {
            return new LifeStealPlayerData().fromNbt(root.getCompound("lifeStealData"));
        }
        return null;
    }

    public void setLifeStealData(LifeStealPlayerData data) {
        if (data == null) {
            root.remove("lifeStealData");
        } else {
            root.put("lifeStealData", data.toNbt(new NbtCompound()));
        }
    }

    public void save() {
        final String reference = holder.getId() + ".dat";
        final Path tmp = dir.resolve(reference + "_tmp");
        final Path cur = dir.resolve(reference);
        final Path old = dir.resolve(reference + "_old");

        try (final OutputStream stream = Files.newOutputStream(tmp)) {
            NbtIo.writeCompressed(root, stream);
            Util.backupAndReplace(cur, tmp, old);
        } catch (IOException ioe) {
            logger.warn("Cannot save data for {}", holder, ioe);
        }
    }
}
