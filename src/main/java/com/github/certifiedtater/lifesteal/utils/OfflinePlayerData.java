package com.github.certifiedtater.lifesteal.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Ampflower
 * @author QPCrummer
 */
public class OfflinePlayerData {

    public final GameProfile holder;
    public final NbtCompound root;

    private final Path dir;

    private static final Logger logger = LogUtils.getLogger();

    protected OfflinePlayerData(GameProfile holder, NbtCompound root, Path dir) {
        this.holder = holder;
        this.root = root;
        this.dir = dir;
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

    /**
     * Gets the player data from the SaveHandler
     *
     * @param server  The Minecraft Server
     * @param profile The profile of the player being fetched
     * @return The offline player's data if it exists and can be read, null otherwise
     */
    public static OfflinePlayerData getOfflinePlayerData(MinecraftServer server, GameProfile profile) {
        final Path dir = server.getSavePath(WorldSavePath.PLAYERDATA);
        final Path dat = dir.resolve(profile.getId() + ".dat");
        if (Files.exists(dat) && Files.isRegularFile(dat)) {
            try (final InputStream stream = Files.newInputStream(dat)) {
                final NbtCompound compound = NbtIo.readCompressed(stream, NbtSizeTracker.ofUnlimitedBytes());
                return new OfflinePlayerData(profile, compound, dir);
            } catch (IOException ioe) {
                logger.warn("Unable to read NBT for {}", profile, ioe);
            }
        }
        return null;
    }

    public void setPosition(ServerWorld world, Vec3d pos) {
        NbtList nbtPos = this.root.getList("Pos", NbtElement.DOUBLE_TYPE);
        nbtPos.set(0, NbtDouble.of(pos.getX()));
        nbtPos.set(1, NbtDouble.of(pos.getY()));
        nbtPos.set(2, NbtDouble.of(pos.getZ()));
        this.root.putString("Dimension", world.getRegistryKey().getValue().toString());
    }

    public void setNewlyRevived(boolean set) {
        this.root.putBoolean("newly_revived", set);
    }

    public void setMaxHealth(double health) {
        NbtList nbtAttributes = this.root.getList("Attributes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtAttributes.size(); i++) {
            NbtCompound compound = nbtAttributes.getCompound(i);
            if (Objects.equals(compound.getString("Name"), "minecraft:generic.max_health")) {
                compound.putDouble("Base", health);
                return;
            }
        }
        NbtCompound compound = new NbtCompound();
        compound.putDouble("Base", health);
        compound.putString("Name", "minecraft:generic.max_health");
        nbtAttributes.add(compound);
    }

    public double getMaxHealth() {
        NbtList nbtAttributes = this.root.getList("Attributes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtAttributes.size(); i++) {
            NbtCompound compound = nbtAttributes.getCompound(i);
            if (Objects.equals(compound.getString("Name"), "minecraft:generic.max_health")) {
                return compound.getDouble("Base");
            }
        }
        return 20.0; // If it doesn't exist, assume it is default
    }

    public void setGamemode(GameMode gamemode) {
        this.root.putInt("playerGameType", gamemode.getId());
    }
}
