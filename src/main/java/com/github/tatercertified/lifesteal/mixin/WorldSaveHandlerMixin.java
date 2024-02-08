package com.github.tatercertified.lifesteal.mixin;

import com.github.tatercertified.lifesteal.util.PlayerDataInterface;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import net.minecraft.world.WorldSaveHandler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.UUID;

@Mixin(WorldSaveHandler.class)
public class WorldSaveHandlerMixin implements PlayerDataInterface {

    @Shadow @Final private File playerDataDir;

    @Shadow @Final private static Logger LOGGER;
    private NbtCompound getPlayerData(UUID uuid, String playerName) {
        NbtCompound nbtCompound = null;

        try {
            File file = new File(this.playerDataDir, uuid.toString() + ".dat");
            if (file.exists() && file.isFile()) {
                nbtCompound = NbtIo.readCompressed(file);
            }
        } catch (Exception var4) {
            LOGGER.warn("Failed to load player data for {}", playerName);
        }

        return nbtCompound;
    }

    private void savePlayerData(UUID uuid, String playerName, NbtCompound compound) {
        try {
            File file = File.createTempFile(uuid.toString() + "-", ".dat", this.playerDataDir);
            NbtIo.writeCompressed(compound, file);
            File file2 = new File(this.playerDataDir, uuid + ".dat");
            File file3 = new File(this.playerDataDir, uuid + ".dat_old");
            Util.backupAndReplace(file2, file, file3);
        } catch (Exception var6) {
            LOGGER.warn("Failed to save player data for {}", playerName);
        }
    }

    @Override
    public NbtCompound getLifeStealInfo(UUID uuid, String playerName) {
        NbtCompound nbtCompound = null;

        try {
            File file = new File(this.playerDataDir, uuid.toString() + ".dat");
            if (file.exists() && file.isFile()) {
                nbtCompound = NbtIo.readCompressed(file);
            }
        } catch (Exception var4) {
            LOGGER.warn("Failed to load player data for {}", playerName);
        }

        if (nbtCompound != null) {
            return nbtCompound.getCompound("lifeStealData");
        }

        return null;
    }

    @Override
    public void saveLifeStealInfo(UUID uuid, String playerName, NbtCompound lifeStealData) {
        NbtCompound compound = getPlayerData(uuid, playerName);
        compound.put("lifeStealData", lifeStealData);
        savePlayerData(uuid, playerName, compound);
    }
}
