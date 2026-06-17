package com.github.tatercertified.lifesteal.utils;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
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

    public final NameAndId holder;
    public final CompoundTag root;

    private final Path dir;

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     *
     * @param holder The GameProfile for the offline player
     * @param root The main NBTCompound for the offline player
     * @param dir The path to the offline player's NBT data
     */
    protected OfflinePlayerData(NameAndId holder, CompoundTag root, Path dir) {
        this.holder = holder;
        this.root = root;
        this.dir = dir;
    }

    /**
     * Saves the offline player's NBT data
     */
    public void save() {
        final String reference = holder.id() + ".dat";
        final Path tmp = dir.resolve(reference + "_tmp");
        final Path cur = dir.resolve(reference);
        final Path old = dir.resolve(reference + "_old");

        try (final OutputStream stream = Files.newOutputStream(tmp)) {
            NbtIo.writeCompressed(root, stream);
            Util.safeReplaceFile(cur, tmp, old);
        } catch (IOException ioe) {
            LOGGER.warn("Cannot save data for {}", holder, ioe);
        }
    }

    /**
     * Gets the player data from the SaveHandler
     *
     * @param server  The Minecraft Server
     * @param profile The profile of the player being fetched
     * @return The offline player's data if it exists and can be read, null otherwise
     */
    public static OfflinePlayerData getOfflinePlayerData(MinecraftServer server, NameAndId profile) {
        final Path dir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        final Path dat = dir.resolve(profile.id() + ".dat");
        if (Files.exists(dat) && Files.isRegularFile(dat)) {
            try (final InputStream stream = Files.newInputStream(dat)) {
                final CompoundTag compound = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
                return new OfflinePlayerData(profile, compound, dir);
            } catch (IOException ioe) {
                LOGGER.warn("Unable to read NBT for {}", profile, ioe);
            }
        }
        return null;
    }

    /**
     * Sets the location of the offline player
     * @param world The ServerWorld that the player should be placed in
     * @param pos The location inside of that world the player should be placed at
     */
    public void setPosition(ServerLevel world, Vec3 pos) {
        ListTag nbtPos = this.root.getListOrEmpty("Pos");
        nbtPos.set(0, DoubleTag.valueOf(pos.x()));
        nbtPos.set(1, DoubleTag.valueOf(pos.y()));
        nbtPos.set(2, DoubleTag.valueOf(pos.z()));
        this.root.putString("Dimension", world.dimension().identifier().toString());
    }

    /**
     * Sets the newly_revived indicator NBT
     * @param set The status of the indicator (true/false)
     */
    public void setNewlyRevived(boolean set) {
        this.root.putBoolean("newly_revived", set);
    }

    /**
     * Sets the max hearts of an offline player
     * @param hearts The new max number of hearts
     */
    public void setMaxHearts(int hearts) {
        double health = hearts * 2.0;
        ListTag nbtAttributes = this.root.getListOrEmpty("Attributes");
        for (int i = 0; i < nbtAttributes.size(); i++) {
            CompoundTag compound = nbtAttributes.getCompoundOrEmpty(i);
            if (Objects.equals(compound.getString("Name").orElse(null), "minecraft:generic.max_health")) {
                compound.putDouble("Base", health);
                return;
            }
        }
        CompoundTag compound = new CompoundTag();
        compound.putDouble("Base", health);
        compound.putString("Name", "minecraft:generic.max_health");
        nbtAttributes.add(compound);
    }

    /**
     * Gets the offline player's max hearts
     * @return the offline player's max hearts
     */
    public int getMaxHearts() {
        ListTag nbtAttributes = this.root.getListOrEmpty("Attributes");
        for (int i = 0; i < nbtAttributes.size(); i++) {
            CompoundTag compound = nbtAttributes.getCompoundOrEmpty(i);
            if (Objects.equals(compound.getString("Name").orElse(null), "minecraft:generic.max_health")) {
                double health = compound.getDouble("Base").orElse(20.0);
                return (int) health / 2;
            }
        }
        return 10; // If it doesn't exist, assume it is default
    }

    /**
     * Sets the offline player's gamemode
     * @param gamemode The GameMode to set for the offline player
     */
    public void setGamemode(GameType gamemode) {
        this.root.putInt("playerGameType", gamemode.getId());
    }
}
