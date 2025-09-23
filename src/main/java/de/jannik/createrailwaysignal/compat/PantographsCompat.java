package de.jannik.createrailwaysignal.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class PantographsCompat {
    public static final String MODID = "pantographsandwires";

    // Expand this list if you want more IDs to count as “supports”
    private static final List<Identifier> SUPPORT_IDS = List.of(
            id("lattice_mast"),
            id("steel_mast"),
            id("wood_mast"),
            id("power_line_bracket"),
            id("cantilever_bracket")
    );

    private PantographsCompat() {}

    private static Identifier id(String path) { return new Identifier(MODID, path); }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(MODID);
    }

    /** True if the state is one of P&W's known support blocks. */
    public static boolean isSupport(BlockState state) {
        if (!isLoaded()) return false;
        Block b = state.getBlock();
        for (Identifier ident : SUPPORT_IDS) {
            Optional<Block> reg = Registries.BLOCK.getOrEmpty(ident);
            if (reg.isPresent() && state.isOf(reg.get())) return true;
        }
        return false;
    }

    /**
     * Try to read the absolute Y rotation in degrees from IRotatableBlock.
     * Returns Optional.empty() if the mod or method is unavailable.
     *
     * FQCN: de.mrjulsen.paw.block.abstractions.IRotatableBlock
     */
    public static Optional<Float> getYRotation(BlockState state) {
        if (!isLoaded()) return Optional.empty();
        try {
            Class<?> iface = Class.forName("de.mrjulsen.paw.block.abstractions.IRotatableBlock");
            Object block = state.getBlock();
            if (!iface.isInstance(block)) return Optional.empty();

            // Avoid signature mismatches by searching by name only:
            Method m = Arrays.stream(iface.getMethods())
                    .filter(me -> me.getName().equals("getYRotation") && me.getParameterCount() == 1)
                    .findFirst().orElse(null);
            if (m == null) {
                // fall back to relative rotation if absolute is not present
                m = Arrays.stream(iface.getMethods())
                        .filter(me -> me.getName().equals("getRelativeYRotation") && me.getParameterCount() == 1)
                        .findFirst().orElse(null);
            }
            if (m == null) return Optional.empty();

            Object val = m.invoke(block, state);
            if (val instanceof Number n) return Optional.of(n.floatValue());
            return Optional.empty();
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    /** Convenience: check several candidate positions based on rotation buckets. */
    public static boolean hasSupportBehindConsideringRotation(World world, BlockPos pos, Direction facing) {
        Direction back = facing.getOpposite();

        // primary: straight behind
        if (isSupport(world.getBlockState(pos.offset(back)))) return true;

        // diagonals for ~22.5°/~45° buckets
        var left  = facing.rotateYCounterclockwise();
        var right = facing.rotateYClockwise();

        // read rotation if available
        BlockState backState = world.getBlockState(pos.offset(back));
        Optional<Float> deg = getYRotation(backState);

        if (deg.isPresent()) {
            float a = normalize(deg.get()); // 0..360
            // treat anything in [10°, 35°] or [145°, 170°] etc. as “diagonal-ish”
            if (isDiagBucket(a)) {
                if (isSupport(world.getBlockState(pos.offset(back).offset(left))))  return true;
                if (isSupport(world.getBlockState(pos.offset(back).offset(right)))) return true;
            }
            // if they ever use ~45°, this catches it too
            if (isStrongDiagBucket(a)) {
                // try a two-step diagonal if you need it (often not necessary):
                if (isSupport(world.getBlockState(pos.offset(back).offset(left).offset(back))))  return true;
                if (isSupport(world.getBlockState(pos.offset(back).offset(right).offset(back)))) return true;
            }
        } else {
            // No angle info? Fall back to simple diagonals (helps with 22.5° layouts).
            if (isSupport(world.getBlockState(pos.offset(back).offset(left))))  return true;
            if (isSupport(world.getBlockState(pos.offset(back).offset(right)))) return true;
        }
        return false;
    }

    private static float normalize(float deg) {
        deg %= 360f;
        if (deg < 0) deg += 360f;
        return deg;
    }

    private static boolean in(float x, float lo, float hi) { return x >= lo && x <= hi; }

    private static boolean isDiagBucket(float a) {
        // near 22.5°/337.5° etc.
        return in(a, 10f, 35f) || in(a, 155f, 200f) || in(a, 345f, 360f) || in(a, 0f, 15f);
    }

    private static boolean isStrongDiagBucket(float a) {
        // near 45°/135°/225°/315°
        return in(a, 35f, 55f) || in(a, 125f, 145f) || in(a, 215f, 235f) || in(a, 305f, 325f);
    }
}
