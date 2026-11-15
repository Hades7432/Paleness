package ua.hades7432.paleness;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opt-in screenshot lab. Does NOTHING in normal gameplay.
 * Only enabled by -Dpaleness.visualTest=true in the isolated CI world.
 */
@EventBusSubscriber(modid = Paleness.ID, value = Dist.CLIENT)
public final class VisualTestClient {
    private static final boolean ACTIVE = Boolean.getBoolean("paleness.visualTest");
    private static int ticks = 0;
    private static boolean constructed = false;
    private static BlockPos base;
    private static boolean capture1, capture2, capture3;

    private VisualTestClient() {}

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (!ACTIVE) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        if (++ticks == 18 && !constructed) {
            constructed = true;
            MinecraftServer server = mc.getSingleplayerServer();
            UUID uuid = mc.player.getUUID();
            server.execute(() -> prepare(server, uuid));
        }
        if (!constructed) return;
        // Capture the gallery, a close-up, active machinery and the real GUI
        // before the runtime-test mod closes its short-lived single-player world.
        if (ticks == 33) shot(mc, "paleness-gallery.png");
        if (ticks == 40) moveCamera(mc, -5, 4.2);
        if (ticks == 49) shot(mc, "paleness-analyzer-idle.png");
        if (ticks == 54) {
            MinecraftServer server = mc.getSingleplayerServer();
            if (server != null && base != null) server.execute(() -> activate(server));
        }
        if (ticks == 65) shot(mc, "paleness-analyzer-working.png");
        if (ticks == 70) {
            MinecraftServer server = mc.getSingleplayerServer();
            if (server != null && base != null) server.execute(() -> openAnalyzer(server, mc.player.getUUID()));
        }
        if (ticks == 81) shot(mc, "paleness-analyzer-gui.png");

    }

    private static void moveCamera(Minecraft mc, int offsetX, double zDistance) {
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null || base == null || mc.player == null) return;
        UUID uuid = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null)
                player.connection.teleport(base.getX()+offsetX+0.5,
                        base.getY()+2.0,base.getZ()+zDistance,180f,12f);
        });
    }

    private static void shot(Minecraft mc, String name) {
        try {
            Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> {});
            System.out.println("[Paleness Test Lab] Screenshot requested: " + name);
        } catch (Exception e) {
            System.err.println("[Paleness Test Lab] Screenshot failed: " + e);
        }
    }

    private static void prepare(MinecraftServer server, UUID uuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player == null) return;
        ServerLevel level = player.serverLevel();
        BlockPos original = player.blockPosition();
        base = new BlockPos(original.getX(), Math.max(110, original.getY() + 12), original.getZ());
        // This is an ephemeral isolated CI world: clear the rendering stage and add a floor.
        for (int dx = -10; dx <= 10; dx++)
            for (int dz = -9; dz <= 13; dz++) {
                level.setBlock(base.offset(dx, -1, dz), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                for (int dy = 0; dy <= 9; dy++)
                    level.setBlock(base.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
            }
        BlockState analyzer = Paleness.ANALYZER.get().defaultBlockState()
            .setValue(MachineBlock.FACING, Direction.SOUTH);
        BlockState condenser = Paleness.CONDENSER.get().defaultBlockState()
            .setValue(MachineBlock.FACING, Direction.SOUTH);
        BlockState infuser = PaleContent.INFUSER.get().defaultBlockState()
            .setValue(InfuserBlock.FACING, Direction.SOUTH);
        level.setBlock(base.offset(-5, 0, 0), analyzer, 3);
        level.setBlock(base.offset(-1, 0, 0), condenser, 3);
        level.setBlock(base.offset(3, 0, 0), infuser, 3);
        level.setBlock(base.offset(7, 0, 0), PaleContent.SUPER_QUARTZ.get().defaultBlockState(), 3);
        level.setDayTime(6000);
        level.setWeatherParameters(6000, 0, false, false);
        player.connection.teleport(base.getX() + 0.5, base.getY() + 2.2,
                base.getZ() + 9.5, 180f, 15f);
        System.out.println("[Paleness Test Lab] Test gallery placed in isolated world.");
    }

    private static void activate(MinecraftServer server) {
        ServerLevel level = server.overworld();
        if (base == null) return;
        BlockPos p1=base.offset(-5,0,0), p2=base.offset(-1,0,0), p3=base.offset(3,0,0);
        for (BlockPos pos : new BlockPos[]{p1,p2})
            if (level.getBlockState(pos).getBlock() instanceof MachineBlock)
                level.setBlock(pos, level.getBlockState(pos).setValue(MachineBlock.PHASE, 3), 3);
        if (level.getBlockState(p3).getBlock() instanceof InfuserBlock)
            level.setBlock(p3, level.getBlockState(p3).setValue(InfuserBlock.PHASE, 3), 3);
    }

    private static void openAnalyzer(MinecraftServer server, UUID uuid) {
        if (base == null) return;
        ServerPlayer p = server.getPlayerList().getPlayer(uuid);
        if (p != null && server.overworld().getBlockEntity(base.offset(-5, 0, 0))
                instanceof MachineEntity machine)
            p.openMenu(machine, buf -> buf.writeBlockPos(base.offset(-5, 0, 0)));
    }
}
