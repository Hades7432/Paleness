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
 * Horizontal 3x3 gateway with a 12-block frame (corners are optional).
 * Destination generation respects the existing terrain: no artificial 15x15 platform.
 */
public final class PalePortalLogic {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath(Paleness.ID,"paleness"));
    private static final String HOME="paleness:overworld_gate";
    private static final String PALE="paleness:dimension_gate";
    private static final String LOCK="paleness:gate_locked_until";
    private static final int COOLDOWN_TICKS=160;
    private PalePortalLogic(){}

    private static BlockPos at(BlockPos northwest,int x,int z){
        return northwest.offset(x,0,z);
    }
    public static boolean frame(Level level,BlockPos northwest){
        for(int x=0;x<5;x++)for(int z=0;z<5;z++){
            if((x==0||x==4)&&(z==0||z==4))continue; // no corners needed
            var state=level.getBlockState(at(northwest,x,z));
            if(x==0||x==4||z==0||z==4){
                if(!state.is(PaleContent.PALE_QUARTZ.get()))return false;
            }else if(!state.isAir()&&!state.is(PaleContent.PORTAL.get()))return false;
        }
        return true;
    }
    public static BlockPos centerOf(Level level,BlockPos inner){
        for(int x=1;x<=3;x++)for(int z=1;z<=3;z++){
            BlockPos northwest=inner.offset(-x,0,-z);
            if(frame(level,northwest))return northwest.offset(2,0,2);
        }
        return null;
    }
    public static boolean canLight(Level level,BlockPos clickedFrame){
        for(int x=0;x<5;x++)for(int z=0;z<5;z++)
            if(frame(level,clickedFrame.offset(-x,0,-z)))return true;
        return false;
    }
    public static boolean light(Level level,BlockPos clickedFrame){
        if(level.isClientSide)return false;
        for(int x=0;x<5;x++)for(int z=0;z<5;z++){
            BlockPos northwest=clickedFrame.offset(-x,0,-z);
            if(!frame(level,northwest))continue;
            for(int ix=1;ix<=3;ix++)for(int iz=1;iz<=3;iz++)
                level.setBlock(at(northwest,ix,iz),PaleContent.PORTAL.get().defaultBlockState(),3);
            return true;
        }
        return false;
    }
    public static boolean validPortal(Level level,BlockPos pos){
        return centerOf(level,pos)!=null;
    }

    private static BlockPos terrainTop(ServerLevel level,int x,int z){
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,new BlockPos(x,0,z));
    }
    /** Prefer naturally level ground. This avoids spawning an elevated constructed platform. */
    private static boolean flatGround(ServerLevel level,BlockPos center){
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            BlockPos top=terrainTop(level,center.getX()+dx,center.getZ()+dz);
            if(Math.abs(top.getY()-center.getY())>1)return false;
            if(!level.getFluidState(top.below()).isEmpty())return false;
            if(!level.getBlockState(top.below()).isSolid())return false;
            if(!level.getBlockState(top.above()).isAir())return false;
        }
        return true;
    }
    /** Find a natural patch within a short radius rather than terraforming a wide square. */
    private static BlockPos chooseGround(ServerLevel level,BlockPos near){
        for(int r=0;r<=32;r+=4){
            for(int dx=-r;dx<=r;dx+=4)for(int dz=-r;dz<=r;dz+=4){
                if(r>0&&Math.abs(dx)<r&&Math.abs(dz)<r)continue;
                BlockPos top=terrainTop(level,near.getX()+dx,near.getZ()+dz);
                if(top.getY()<level.getMinBuildHeight()+4 || top.getY()>level.getMaxBuildHeight()-8)continue;
                if(flatGround(level,top))return top;
            }
        }
        return terrainTop(level,near.getX(),near.getZ());
    }

    /** Builds only 12 frame blocks and 9 membrane tiles, directly on local terrain. */
    static BlockPos makeDestination(ServerLevel level,BlockPos near){
        BlockPos center=chooseGround(level,near);
        BlockPos northwest=center.offset(-2,0,-2);
        for(int x=0;x<5;x++)for(int z=0;z<5;z++){
            if((x==0||x==4)&&(z==0||z==4))continue;
            BlockPos p=at(northwest,x,z);
            // Minimal local support only where a 1-block height difference creates a gap.
            if(level.getBlockState(p.below()).isAir())
                level.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);
            if(x==0||x==4||z==0||z==4)
                level.setBlock(p,PaleContent.PALE_QUARTZ.get().defaultBlockState(),3);
        }
        for(int x=1;x<=3;x++)for(int z=1;z<=3;z++)
            level.setBlock(at(northwest,x,z),PaleContent.PORTAL.get().defaultBlockState(),3);
        return center;
    }

    /** Find solid natural ground >=3 blocks away, at the correct local surface height. */
    static BlockPos safeLanding(ServerLevel level,BlockPos center){
        for(int dist:new int[]{3,4,5,6})
            for(int[] dir:new int[][]{{1,0},{0,1},{-1,0},{0,-1}}){
                BlockPos feet=terrainTop(level,center.getX()+dir[0]*dist,center.getZ()+dir[1]*dist);
                if(Math.abs(feet.getY()-center.getY())>4)continue;
                if(level.getBlockState(feet).isAir()
                    && level.getBlockState(feet.above()).isAir()
                    && level.getBlockState(feet.below()).isSolid()
                    && level.getFluidState(feet.below()).isEmpty())return feet;
            }
        // Single safe tile fallback, not a 15x15 platform (and no forced hover).
        BlockPos feet=center.offset(3,0,0);
        level.setBlock(feet.below(),Blocks.STONE.defaultBlockState(),3);
        level.setBlock(feet,Blocks.AIR.defaultBlockState(),3);
        level.setBlock(feet.above(),Blocks.AIR.defaultBlockState(),3);
        return feet;
    }

    public static void travel(ServerPlayer player,BlockPos touched){
        ServerLevel source=player.serverLevel();
        if(player.isOnPortalCooldown())return;
        CompoundTag data=player.getPersistentData();
        if(source.getGameTime()<data.getLong(LOCK))return;
        BlockPos sourceCenter=centerOf(source,touched);
        if(sourceCenter==null)return;
        boolean returning=source.dimension().equals(DIMENSION);
        ServerLevel target=source.getServer().getLevel(returning?Level.OVERWORLD:DIMENSION);
        if(target==null)return;

        if(!returning)data.putLong(HOME,sourceCenter.asLong());
        String linkKey=returning?HOME:PALE;
        long savedPos=data.getLong(linkKey);
        BlockPos saved=BlockPos.of(savedPos);
        BlockPos targetCenter=savedPos!=0
                && target.getBlockState(saved).is(PaleContent.PORTAL.get())
                && validPortal(target,saved) ? saved
                : makeDestination(target,sourceCenter);
        if(!returning)data.putLong(PALE,targetCenter.asLong());
        BlockPos feet=safeLanding(target,targetCenter);

        player.setPortalCooldown();
        Vec3 destination=Vec3.atBottomCenterOf(feet);
        ServerPlayer moved=(ServerPlayer)player.changeDimension(new DimensionTransition(
            target,destination,Vec3.ZERO,player.getYRot(),player.getXRot(),
            false,DimensionTransition.PLAY_PORTAL_SOUND));
        if(moved!=null){
            moved.setPortalCooldown();
            moved.getPersistentData().putLong(LOCK,target.getGameTime()+COOLDOWN_TICKS);
        }
    }
}
