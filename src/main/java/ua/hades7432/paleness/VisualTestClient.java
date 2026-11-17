package ua.hades7432.paleness;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
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
        if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.screen!=null)return;
        if(++ticks==18&&!prepared){
            prepared=true;
            MinecraftServer s=mc.getSingleplayerServer();
            UUID uuid=mc.player.getUUID();
            s.execute(()->prepare(s,uuid));
        }
        if(!prepared)return;
        if(ticks==31)capture(mc,"paleness-01-ore-metal-gallery.png");
        // Keep the 2×2 quartz group fully within the next screenshot.
        if(ticks==35)teleport(mc,2,5,32);
        if(ticks==41)capture(mc,"paleness-02-pale-quartz-connected.png");
        if(ticks==44)teleport(mc,2,4,12);
        if(ticks==50)capture(mc,"paleness-03-pale-quartz-closeup.png");
        if(ticks==52)teleport(mc,0,1,15);
        if(ticks==58)capture(mc,"paleness-04-soul-tagger-in-hand.png");
        if(ticks==64)teleportPortal(mc,0,0,5.2,180f,36f);
        if(ticks==66)capture(mc,"paleness-05-portal-b.png");
        if(ticks==71)teleportPortal(mc,0,0,5.2,180f,36f);
        if(ticks==73)capture(mc,"paleness-06-portal-b-animation.png");
        if(ticks==78)teleportPortal(mc,3,0,5.2,153f,32f);
        if(ticks==80)capture(mc,"paleness-07-soul-tagger-second-view.png");
    }
    private static void capture(Minecraft mc,String name){
        Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->{});
        System.out.println("[Paleness Visual 0.6] "+name);
    }
    private static void teleport(Minecraft mc,int dx,double dz,float pitch){
        MinecraftServer server=mc.getSingleplayerServer();
        UUID uuid=mc.player.getUUID();
        if(base==null)return;
        server.execute(()->{
            ServerPlayer player=server.getPlayerList().getPlayer(uuid);
            if(player!=null)player.connection.teleport(base.getX()+dx+.5,
                base.getY()+2.3,base.getZ()+dz,180f,pitch);
        });
    }
    private static void teleportPortal(Minecraft mc,int dx,int dz,double cameraHeight,float yaw,float pitch){
        MinecraftServer server=mc.getSingleplayerServer();
        UUID uuid=mc.player.getUUID();
        if(base==null)return;
        server.execute(()->{
            ServerPlayer player=server.getPlayerList().getPlayer(uuid);
            if(player!=null)player.connection.teleport(base.getX()+dx+0.5,
                base.getY()+cameraHeight,base.getZ()+dz+0.5,yaw,pitch);
        });
    }
    private static void prepare(MinecraftServer server,UUID uuid){
        ServerPlayer player=server.getPlayerList().getPlayer(uuid);
        if(player==null)return;
        ServerLevel level=player.serverLevel();
        BlockPos initial=player.blockPosition();
        base=new BlockPos(initial.getX(),Math.max(110,initial.getY()+12),initial.getZ());
        for(int dx=-11;dx<=11;dx++)for(int dz=-12;dz<=14;dz++){
            level.setBlock(base.offset(dx,-1,dz),Blocks.SMOOTH_STONE.defaultBlockState(),3);
            for(int dy=0;dy<=7;dy++)
                level.setBlock(base.offset(dx,dy,dz),Blocks.AIR.defaultBlockState(),3);
        }
        level.setBlock(base.offset(-6,0,0),PaleMetalContent.PALE_ORE.get().defaultBlockState(),3);
        level.setBlock(base.offset(-2,0,0),PaleMetalContent.PALE_METAL_BLOCK.get().defaultBlockState(),3);
        for(int x=0;x<2;x++)for(int z=0;z<2;z++)
            level.setBlock(base.offset(2+x,0,z),PaleQuartzBlock.connectedState(level,base.offset(2+x,0,z)),3);
        BlockPos northwest=base.offset(-2,0,-8);
        for(int x=0;x<5;x++)for(int z=0;z<5;z++){
            if((x==0||x==4)&&(z==0||z==4))continue;
            if(x==0||x==4||z==0||z==4)
                level.setBlock(northwest.offset(x,0,z),
                    PaleQuartzBlock.connectedState(level,northwest.offset(x,0,z)),3);
        }
        PalePortalLogic.light(level,northwest.offset(0,0,2));
        player.setItemInHand(InteractionHand.MAIN_HAND,
            new ItemStack(PaleContent.SOUL_TAGGER.get()));
        level.setDayTime(6000);
        level.setWeatherParameters(6000,0,false,false);
        // Start facing the Ore (-6, 0) and Metal (-2, 0) gallery,
        // rather than the portal on the far side of the lab.
        player.connection.teleport(base.getX()-3.5,base.getY()+2.3,
            base.getZ()+5.5,180f,32f);
    }
}
