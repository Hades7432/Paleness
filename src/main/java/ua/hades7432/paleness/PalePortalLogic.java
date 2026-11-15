package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/**
 * End-like HORIZONTAL Paleness gate: 3x3 opening inside 12 super-pale quartz
 * frame blocks, corners are intentionally not needed.
 */
public final class PalePortalLogic {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Paleness.ID, "paleness"));
    private static final String HOME = "paleness:overworld_gate";
    private static final String PALE = "paleness:dimension_gate";
    private static final String LOCK = "paleness:gate_locked_until";
    private static final int DISTANCE = 4;
    private static final int COOLDOWN_TICKS = 160;
    private PalePortalLogic() {}

    private static BlockPos at(BlockPos nw, int x, int z) {
        return nw.offset(x, 0, z);
    }

    // NW corner is an imaginary coordinate; no corner blocks are required.
    public static boolean frame(Level level, BlockPos nw) {
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) {
            if ((x == 0 || x == 4) && (z == 0 || z == 4)) continue;
            BlockPos pos = at(nw, x, z);
            if (x == 0 || x == 4 || z == 0 || z == 4) {
                if (!level.getBlockState(pos).is(PaleContent.SUPER_QUARTZ.get())) return false;
            } else {
                var state = level.getBlockState(pos);
                if (!state.isAir() && !state.is(PaleContent.PORTAL.get())) return false;
            }
        }
        return true;
    }

    public static BlockPos centerOf(Level level, BlockPos inner) {
        for (int x = 1; x <= 3; x++) for (int z = 1; z <= 3; z++) {
            BlockPos nw = inner.offset(-x, 0, -z);
            if (frame(level, nw)) return nw.offset(2, 0, 2);
        }
        return null;
    }

    public static boolean light(Level level, BlockPos clickedFrame) {
        if (level.isClientSide) return false;
        for (int x = 0; x <= 4; x++) for (int z = 0; z <= 4; z++) {
            BlockPos nw = clickedFrame.offset(-x, 0, -z);
            if (!frame(level, nw)) continue;
            // All 12 frame blocks are already present before any portal block is placed.
            for (int ix = 1; ix <= 3; ix++) for (int iz = 1; iz <= 3; iz++)
                level.setBlock(at(nw, ix, iz), PaleContent.PORTAL.get().defaultBlockState(), 3);
            return true;
        }
        return false;
    }

    public static boolean validPortal(Level level, BlockPos pos) {
        return centerOf(level, pos) != null;
    }

    /** An isolated destination gate and clear stone safety platform. */
    private static BlockPos makeDestination(ServerLevel level, BlockPos near) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(near.getX(), 0, near.getZ()));
        int y = Math.clamp(surface.getY() + 5, 74, level.getMaxBuildHeight() - 12);
        BlockPos center = new BlockPos(near.getX(), y, near.getZ());
        BlockPos nw = center.offset(-2, 0, -2);
        // Solid landing platform under the gate, extending out to safe dismounts.
        for (int dx = -7; dx <= 7; dx++) for (int dz = -7; dz <= 7; dz++) {
            level.setBlock(center.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), 3);
            if (Math.abs(dx) > 2 || Math.abs(dz) > 2)
                level.setBlock(center.offset(dx, 0, dz), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            for (int dy = 1; dy <= 3; dy++)
                level.setBlock(center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
        }
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) {
            if ((x == 0 || x == 4) && (z == 0 || z == 4)) continue;
            if (x == 0 || x == 4 || z == 0 || z == 4)
                level.setBlock(at(nw, x, z), PaleContent.SUPER_QUARTZ.get().defaultBlockState(), 3);
        }
        for (int x = 1; x <= 3; x++) for (int z = 1; z <= 3; z++)
            level.setBlock(at(nw, x, z), PaleContent.PORTAL.get().defaultBlockState(), 3);
        return center;
    }

    private static BlockPos safeLanding(ServerLevel level, BlockPos center) {
        for (int dist : new int[]{DISTANCE, DISTANCE + 1, DISTANCE + 2}) {
            for (int[] dir : new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}}) {
                BlockPos feet = center.offset(dir[0] * dist, 1, dir[1] * dist);
                if (level.getBlockState(feet).isAir()
                        && level.getBlockState(feet.above()).isAir()
                        && level.getBlockState(feet.below()).isSolid())
                    return feet;
            }
        }
        // Repair a limited safe arrival area only as a last resort.
        BlockPos feet = center.offset(DISTANCE, 1, 0);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            level.setBlock(feet.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            level.setBlock(feet.offset(x, 0, z), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(feet.offset(x, 1, z), Blocks.AIR.defaultBlockState(), 3);
        }
        return feet;
    }

    public static void travel(ServerPlayer player, BlockPos touched) {
        ServerLevel origin = player.serverLevel();
        if (player.isOnPortalCooldown()) return;
        CompoundTag data = player.getPersistentData();
        if (origin.getGameTime() < data.getLong(LOCK)) return;
        if (!validPortal(origin, touched)) return;
        BlockPos sourceCenter = centerOf(origin, touched);
        if (sourceCenter == null) return;

        boolean returning = origin.dimension().equals(DIMENSION);
        ServerLevel target = origin.getServer().getLevel(returning ? Level.OVERWORLD : DIMENSION);
        if (target == null) return;

        if (!returning) data.putLong(HOME, sourceCenter.asLong());
        String key = returning ? HOME : PALE;
        long old = data.getLong(key);
        BlockPos saved = BlockPos.of(old);
        BlockPos destCenter = old != 0 && target.getBlockState(saved).is(PaleContent.PORTAL.get())
                && validPortal(target, saved) ? saved
                : makeDestination(target, returning ? sourceCenter : sourceCenter);
        if (!returning) data.putLong(PALE, destCenter.asLong());

        BlockPos feet = safeLanding(target, destCenter);
        // Cross-dimension cooldown, including Creative and simultaneous portal blocks.
        data.putLong(LOCK, target.getGameTime() + COOLDOWN_TICKS);
        player.setPortalCooldown();
        Vec3 destination = Vec3.atBottomCenterOf(feet);
        ServerPlayer moved = (ServerPlayer) player.changeDimension(
                new DimensionTransition(target, destination, Vec3.ZERO,
                        player.getYRot(), player.getXRot(), false, DimensionTransition.PLAY_PORTAL_SOUND));
        if (moved != null) {
            moved.setPortalCooldown();
            moved.getPersistentData().putLong(LOCK, target.getGameTime() + COOLDOWN_TICKS);
        }
    }
}
