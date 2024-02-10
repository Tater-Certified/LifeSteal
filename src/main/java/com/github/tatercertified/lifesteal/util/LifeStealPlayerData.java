package com.github.tatercertified.lifesteal.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * @author Ampflower
 * @since 1.4.0
 **/
public class LifeStealPlayerData {
    public String reviver;
    public BlockPos teleport;
    public Identifier dimension;

    public LifeStealPlayerData() {
    }

    public LifeStealPlayerData(String reviver, BlockPos teleport, Identifier dimension) {
        this.reviver = reviver;
        this.teleport = teleport;
        this.dimension = dimension;
    }

    public ServerWorld resolveDimension(MinecraftServer server) {
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, dimension));
    }

    public LifeStealPlayerData fromNbt(NbtCompound compound) {
        if (compound == null) {
            return this;
        }

        this.reviver = compound.getString("reviver");
        if (compound.contains("dimension", NbtElement.STRING_TYPE)) {
            this.dimension = Identifier.tryParse(compound.getString("dimension"));
        } else {
            this.dimension = null;
        }
        if (compound.contains("teleport", NbtElement.INT_ARRAY_TYPE)) {
            this.teleport = toBlockPos(compound.getIntArray("teleport"));
        } else {
            this.teleport = null;
        }

        return this;
    }

    public NbtCompound toNbt(NbtCompound compound) {
        compound.putString("reviver", this.reviver);
        if (this.dimension != null) {
            compound.putString("dimension", this.dimension.toString());
        }
        if (this.teleport != null) {
            compound.putIntArray("teleport", fromBlockPos(teleport));
        }

        return compound;
    }

    private static int[] fromBlockPos(BlockPos pos) {
        return new int[]{
                pos.getX(),
                pos.getY(),
                pos.getZ()
        };
    }

    private static BlockPos toBlockPos(int[] array) {
        final int x = array[0];
        final int y = array[1];
        final int z = array[2];
        return new BlockPos(x, y, z);
    }
}
