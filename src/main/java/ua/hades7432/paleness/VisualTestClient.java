package ua.hades7432.paleness;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=Paleness.ID,value=Dist.CLIENT)
public final class VisualTestClient {
    private static final boolean ACTIVE=Boolean.getBoolean("paleness.visualTest");
    private static int ticks;
    private static BlockPos base;
    private static boolean prepared;
    private VisualTestClient(){}

    @SubscribeEvent public static void onTick(ClientTickEvent.Post event){
        if(!ACTIVE)return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        // Never photograph the 'Loading terrain' overlay.
        if(mc.screen!=null && !(mc.screen instanceof InfuserScreen))return;
        if(++ticks==18&&!prepared){
            prepared=true;
            MinecraftServer s=mc.getSingleplayerServer();
            UUID uuid=mc.player.getUUID();
            s.execute(()->prepare(s,uuid));
        }
        if(!prepared)return;
        // Runtime GameTest closes the Minecraft client shortly after tick 90.
        // All seven screenshots are captured BEFORE the last GUI shot.
        if(ticks==28)capture(mc,"paleness-01-ore-metal-gallery.png");
        if(ticks==31)move(mc,-6,4.0);
        if(ticks==37)capture(mc,"paleness-02-ore-closeup.png");
        if(ticks==39)move(mc,-2,4.0);
        if(ticks==45)capture(mc,"paleness-03-metal-closeup.png");
        if(ticks==47)move(mc,2,4.0);
        if(ticks==53)capture(mc,"paleness-04-infuser-idle.png");
        if(ticks==55) {
            MinecraftServer server=mc.getSingleplayerServer();
            server.execute(()->activate(server));
        }
        if(ticks==60)capture(mc,"paleness-05-infuser-active.png");
        if(ticks==62) {
            MinecraftServer server=mc.getSingleplayerServer();
            UUID uuid=mc.player.getUUID();
            server.execute(()->{
                ServerPlayer player=server.getPlayerList().getPlayer(uuid);
                if(player!=null&&base!=null)player.connection.teleport(
                    base.getX()+0.5,base.getY()+4.7,base.getZ()-2.5,180f,48f);
            });
        }
        if(ticks==70)capture(mc,"paleness-07-horizontal-portal.png");
        if(ticks==72)move(mc,2,4.0);
        if(ticks==77) {
            MinecraftServer server=mc.getSingleplayerServer();
            UUID uuid=mc.player.getUUID();
            server.execute(()->openInfuser(server,uuid));
        }
        if(ticks==85)capture(mc,"paleness-06-infuser-gui.png");
        if(ticks==87&&mc.screen instanceof InfuserScreen) {
            mc.setScreen(null);
            mc.player.closeContainer();
        }
    }

    private static void capture(Minecraft mc,String name){
        Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->{});
        System.out.println("[Paleness Visual 0.5] "+name);
    }
    private static void move(Minecraft mc,int x,double z){
        MinecraftServer s=mc.getSingleplayerServer();
        if(s==null||base==null)return;
        UUID uuid=mc.player.getUUID();
        s.execute(()->{
            ServerPlayer p=s.getPlayerList().getPlayer(uuid);
            if(p!=null)p.connection.teleport(base.getX()+x+.5,
                base.getY()+2.0,base.getZ()+z,180f,12f);
        });
    }
    private static void prepare(MinecraftServer s,UUID uuid){
        ServerPlayer player=s.getPlayerList().getPlayer(uuid);
        if(player==null)return;
        ServerLevel level=player.serverLevel();
        BlockPos initial=player.blockPosition();
        base=new BlockPos(initial.getX(),Math.max(110,initial.getY()+12),initial.getZ());
        for(int dx=-11;dx<=11;dx++)for(int dz=-11;dz<=14;dz++){
            level.setBlock(base.offset(dx,-1,dz),Blocks.SMOOTH_STONE.defaultBlockState(),3);
            for(int dy=0;dy<=7;dy++)
                level.setBlock(base.offset(dx,dy,dz),Blocks.AIR.defaultBlockState(),3);
        }
        level.setBlock(base.offset(-6,0,0),PaleMetalContent.PALE_ORE.get().defaultBlockState(),3);
        level.setBlock(base.offset(-2,0,0),PaleMetalContent.PALE_METAL_BLOCK.get().defaultBlockState(),3);
        level.setBlock(base.offset(2,0,0),PaleContent.INFUSER.get().defaultBlockState()
            .setValue(InfuserBlock.FACING,Direction.SOUTH),3);
        level.setBlock(base.offset(6,0,0),PaleContent.SUPER_QUARTZ.get().defaultBlockState(),3);
        BlockPos northwest=base.offset(-2,0,-8);
        for(int x=0;x<5;x++)for(int z=0;z<5;z++) {
            if((x==0||x==4)&&(z==0||z==4))continue;
            if(x==0||x==4||z==0||z==4)
                level.setBlock(northwest.offset(x,0,z),
                    PaleContent.SUPER_QUARTZ.get().defaultBlockState(),3);
        }
        PalePortalLogic.light(level,northwest.offset(0,0,2));
        player.setItemInHand(InteractionHand.MAIN_HAND,
            new ItemStack(PaleMetalContent.PALENESS_DETECTOR.get()));
        level.setDayTime(6000);
        level.setWeatherParameters(6000,0,false,false);
        player.connection.teleport(base.getX()+.5,base.getY()+2.2,
            base.getZ()+9.5,180f,15f);
    }
    private static void activate(MinecraftServer s){
        if(base==null)return;
        ServerLevel level=s.overworld();
        BlockPos p=base.offset(2,0,0);
        BlockState b=level.getBlockState(p);
        if(b.getBlock() instanceof InfuserBlock)
            level.setBlock(p,b.setValue(InfuserBlock.PHASE,3),3);
    }
    private static void openInfuser(MinecraftServer s,UUID uuid){
        if(base==null)return;
        ServerPlayer p=s.getPlayerList().getPlayer(uuid);
        if(p!=null&&s.overworld().getBlockEntity(base.offset(2,0,0))instanceof InfuserEntity e)
            p.openMenu(e,buf->buf.writeBlockPos(base.offset(2,0,0)));
    }
}
