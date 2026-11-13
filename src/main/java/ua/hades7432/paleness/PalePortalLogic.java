package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public final class PalePortalLogic {
    public static final ResourceKey<Level> DIMENSION=ResourceKey.create(Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath(Paleness.ID,"paleness"));
    private static final String RETURN_KEY="paleness:home_portal";
    private PalePortalLogic(){}

    private static BlockPos at(BlockPos base,Direction.Axis axis,int dx,int dy){
        return axis==Direction.Axis.X ? base.offset(dx,dy,0) : base.offset(0,dy,dx);
    }
    private static boolean frame(Level level,BlockPos base,Direction.Axis axis){
        for(int y=0;y<5;y++)for(int x=0;x<4;x++){
            BlockState state=level.getBlockState(at(base,axis,x,y));
            if(x==0||x==3||y==0||y==4){
                if(!state.is(PaleContent.SUPER_QUARTZ.get()))return false;
            }else if(!state.isAir()&&!state.is(PaleContent.PORTAL.get()))return false;
        }
        return true;
    }
    public static boolean light(Level level,BlockPos clicked){
        if(level.isClientSide)return false;
        for(Direction.Axis axis:new Direction.Axis[]{Direction.Axis.X,Direction.Axis.Z})
            for(int dy=-4;dy<=0;dy++)for(int side=-3;side<=0;side++){
                BlockPos corner=at(clicked,axis,side,dy);
                if(!frame(level,corner,axis))continue;
                for(int y=1;y<=3;y++)for(int x=1;x<=2;x++)
                    level.setBlock(at(corner,axis,x,y),
                        PaleContent.PORTAL.get().defaultBlockState().setValue(PalePortalBlock.AXIS,axis),3);
                return true;
            }
        return false;
    }
    public static boolean validPortal(Level level,BlockPos portalPos,Direction.Axis axis){
        for(int yOffset=-3;yOffset<=-1;yOffset++)for(int xOffset=-2;xOffset<=-1;xOffset++){
            BlockPos base=at(portalPos,axis,xOffset,yOffset);
            if(frame(level,base,axis))return true;
        }
        return false;
    }
    private static BlockPos makeExit(ServerLevel level,BlockPos near,Direction.Axis axis){
        BlockPos heightPos=level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            new BlockPos(near.getX(),0,near.getZ()));
        int y=Math.clamp(heightPos.getY()+1,70,level.getMaxBuildHeight()-8);
        BlockPos base=new BlockPos(near.getX(),y,near.getZ());
        // First prototype: a deliberate stone landing, not an unsafe teleport into caves/water.
        for(int dx=-2;dx<=5;dx++)for(int dz=-2;dz<=5;dz++)
            level.setBlock(base.offset(dx,-1,dz),Blocks.STONE.defaultBlockState(),3);
        for(int yy=0;yy<5;yy++)for(int xx=0;xx<4;xx++){
            BlockPos p=at(base,axis,xx,yy);
            if(xx==0||xx==3||yy==0||yy==4)
                level.setBlock(p,PaleContent.SUPER_QUARTZ.get().defaultBlockState(),3);
            else level.setBlock(p,PaleContent.PORTAL.get().defaultBlockState()
                .setValue(PalePortalBlock.AXIS,axis),3);
        }
        return at(base,axis,1,1);
    }
    public static void travel(ServerPlayer player,BlockPos source,Direction.Axis axis){
        ServerLevel sourceLevel=player.serverLevel();
        boolean comingBack=sourceLevel.dimension().equals(DIMENSION);
        ServerLevel dest=sourceLevel.getServer().getLevel(
            comingBack?Level.OVERWORLD:DIMENSION);
        if(dest==null)return;
        BlockPos landing;
        if(comingBack){
            long home=player.getPersistentData().getLong(RETURN_KEY);
            BlockPos recorded=BlockPos.of(home);
            if(home!=0 && dest.getBlockState(recorded).is(PaleContent.PORTAL.get()))
                landing=recorded;
            else landing=makeExit(dest,home==0?source:recorded,axis);
        }else{
            player.getPersistentData().putLong(RETURN_KEY,source.asLong());
            landing=makeExit(dest,source,axis);
        }
        Vec3 pos=Vec3.atCenterOf(landing).add(0,0.05,0);
        player.setPortalCooldown();
        player.changeDimension(new DimensionTransition(dest,pos,Vec3.ZERO,
            player.getYRot(),player.getXRot(),false,DimensionTransition.PLAY_PORTAL_SOUND));
    }
}