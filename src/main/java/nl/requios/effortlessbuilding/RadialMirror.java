package nl.requios.effortlessbuilding;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

import java.util.ArrayList;
import java.util.List;

public class RadialMirror {

    public static class RadialMirrorSettings {
        public boolean enabled = false;
        public Vec3d position = new Vec3d(0.5, 64.5, 0.5);
        public int slices = 4;
        public boolean alternate = false;
        public int radius = 20;
        public boolean drawLines = true, drawPlanes = false;

        public RadialMirrorSettings() {
        }

        public RadialMirrorSettings(boolean enabled, Vec3d position, int slices, boolean alternate, int radius, boolean drawLines, boolean drawPlanes) {
            this.enabled = enabled;
            this.position = position;
            this.slices = slices;
            this.alternate = alternate;
            this.radius = radius;
            this.drawLines = drawLines;
            this.drawPlanes = drawPlanes;
        }

        public int getReach() {
            return radius * 2;
        }
    }

    public static List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos) {
        List<BlockPos> coordinates = new ArrayList<>();

        //find radial mirror settings for the player
        RadialMirrorSettings r = BuildSettingsManager.getBuildSettings(player).getRadialMirrorSettings();
        if (!isEnabled(r, startPos)) return coordinates;

        //get angle between slices
        double sliceAngle = 2 * Math.PI / r.slices;

        Vec3d startVec = new Vec3d(startPos.getX() + 0.5f, startPos.getY() + 0.5f, startPos.getZ() + 0.5f);
        Vec3d relStartVec = startVec.subtract(r.position);

        double startAngleToCenter = MathHelper.atan2(relStartVec.x, relStartVec.z);
        if (startAngleToCenter < 0) startAngleToCenter += Math.PI;
        double startAngleInSlice = startAngleToCenter % sliceAngle;

        for (int i = 1; i < r.slices; i++) {
            double curAngle = sliceAngle * i;

            //alternate mirroring of slices
            if (r.alternate && i%2 == 1) {
                curAngle = curAngle - startAngleInSlice + (sliceAngle - startAngleInSlice);
            }

            Vec3d relNewVec = relStartVec.rotateYaw((float) curAngle);
            Vec3d newVec = r.position.add(relNewVec);
            coordinates.add(new BlockPos(newVec));
        }

        return coordinates;
    }

    public static List<IBlockState> findBlockStates(EntityPlayer player, BlockPos startPos, IBlockState blockState, ItemStack itemStack, List<ItemStack> itemStacks) {
        List<IBlockState> blockStates = new ArrayList<>();

        //find radial mirror settings for the player that placed the block
        RadialMirrorSettings r = BuildSettingsManager.getBuildSettings(player).getRadialMirrorSettings();
        if (!isEnabled(r, startPos)) return blockStates;


        //get angle between slices
        double sliceAngle = 2 * Math.PI / r.slices;

        Vec3d startVec = new Vec3d(startPos.getX() + 0.5f, startPos.getY() + 0.5f, startPos.getZ() + 0.5f);
        Vec3d relStartVec = startVec.subtract(r.position);

        double startAngleToCenter = MathHelper.atan2(relStartVec.x, relStartVec.z);
        if (startAngleToCenter < 0) startAngleToCenter += Math.PI;
        double startAngleInSlice = startAngleToCenter % sliceAngle;

        //Rotate the original blockstate
        if (startAngleToCenter < -0.75 * Math.PI || startAngleToCenter > 0.75 * Math.PI) {
            blockState = blockState.withRotation(Rotation.CLOCKWISE_180);
        } else if (startAngleToCenter < -0.25 * Math.PI) {
            blockState = blockState.withRotation(Rotation.CLOCKWISE_90);
        } else if (startAngleToCenter > 0.25 * Math.PI) {
            blockState = blockState.withRotation(Rotation.COUNTERCLOCKWISE_90);
        }

        //Randomizer bag synergy
        IItemHandler bagInventory = null;
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemRandomizerBag) {
            bagInventory = ItemRandomizerBag.getBagInventory(itemStack);
        }

        IBlockState newBlockState;
        for (int i = 1; i < r.slices; i++) {
            newBlockState = blockState;
            double curAngle = sliceAngle * i;

            //alternate mirroring of slices
            if (r.alternate && i%2 == 1) {
                curAngle = curAngle - startAngleInSlice + (sliceAngle - startAngleInSlice);
            }

            Vec3d relNewVec = relStartVec.rotateYaw((float) curAngle);
            Vec3d newVec = r.position.add(relNewVec);

            //Randomizer bag synergy
            if (bagInventory != null) {
                itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
                newBlockState = BuildModifiers.getBlockStateFromItem(itemStack, player, startPos, EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
            }

            //rotate
            double angleToCenter = MathHelper.atan2(relNewVec.x, relNewVec.z); //between -PI and PI

            if (angleToCenter < -0.75 * Math.PI || angleToCenter > 0.75 * Math.PI) {
                newBlockState = newBlockState.withRotation(Rotation.CLOCKWISE_180);
                if (r.alternate && i%2 == 1) {
                    newBlockState = newBlockState.withMirror(Mirror.FRONT_BACK);
                }
            } else if (angleToCenter < -0.25 * Math.PI) {
                newBlockState = newBlockState.withRotation(Rotation.CLOCKWISE_90);
                if (r.alternate && i%2 == 1) {
                    newBlockState = newBlockState.withMirror(Mirror.LEFT_RIGHT);
                }
            } else if (angleToCenter > 0.25 * Math.PI) {
                newBlockState = newBlockState.withRotation(Rotation.COUNTERCLOCKWISE_90);
                if (r.alternate && i%2 == 1) {
                    newBlockState = newBlockState.withMirror(Mirror.LEFT_RIGHT);
                }
            } else {
                if (r.alternate && i%2 == 1) {
                    newBlockState = newBlockState.withMirror(Mirror.FRONT_BACK);
                }
            }

            blockStates.add(newBlockState);
            itemStacks.add(itemStack);
        }

        return blockStates;
    }

    public static boolean isEnabled(RadialMirrorSettings r, BlockPos startPos) {
        if (r == null || !r.enabled) return false;

        if (new Vec3d(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5).subtract(r.position).lengthSquared() >
            r.radius * r.radius)
            return false;

        return true;
    }

}
