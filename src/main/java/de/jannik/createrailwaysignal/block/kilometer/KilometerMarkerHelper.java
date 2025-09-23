package de.jannik.createrailwaysignal.block.kilometer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public final class KilometerMarkerHelper {

    private static final TagKey<Block> TAG_MASTS =
            TagKey.of(RegistryKeys.BLOCK, new Identifier("createrailwaysignal", "kilometer_connects_to"));

    private KilometerMarkerHelper() {}

    /** True if this state should count as a connectable mast/pillar. */
    public static boolean isConnectable(BlockState s) {
        return s.isIn(TAG_MASTS);
    }

    /** Simple “behind” check: straight or the two diagonals one block back. */
    public static boolean hasConnectableBehind(BlockView world, BlockPos pos, BlockState markerState) {
        Direction facing = markerState.get(KilometerMarkerBlock.FACING);
        Direction back   = facing.getOpposite();
        Direction left   = facing.rotateYCounterclockwise();
        Direction right  = facing.rotateYClockwise();

        if (world.getBlockState(pos.offset(back)).isIn(TAG_MASTS)) return true;
        if (world.getBlockState(pos.offset(back).offset(left)).isIn(TAG_MASTS)) return true;
        if (world.getBlockState(pos.offset(back).offset(right)).isIn(TAG_MASTS)) return true;
        return false;
    }
}
