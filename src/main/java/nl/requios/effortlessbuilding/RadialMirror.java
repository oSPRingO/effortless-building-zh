package nl.requios.effortlessbuilding;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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

        //get angle
        float angle = 2f * ((float) Math.PI) / r.slices;

        Vec3d startVec = new Vec3d(startPos.getX() + 0.5f, startPos.getY() + 0.5f, startPos.getZ() + 0.5f);
        Vec3d relStartVec = startVec.subtract(r.position);

        for (int i = 1; i < r.slices; i++) {
            float curAngle = angle * i;
            if (r.alternate) {
                //TODO alternate
                // get angle in slice
            }
            Vec3d relNewVec = relStartVec.rotateYaw(curAngle);
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


        //get angle
        float angle = 2f * ((float) Math.PI) / r.slices;

        Vec3d startVec = new Vec3d(startPos.getX() + 0.5f, startPos.getY() + 0.5f, startPos.getZ() + 0.5f);
        Vec3d relStartVec = startVec.subtract(r.position);

        //Randomizer bag synergy
        IItemHandler bagInventory = null;
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemRandomizerBag) {
            bagInventory = ItemRandomizerBag.getBagInventory(itemStack);
        }

        for (int i = 1; i < r.slices; i++) {
            Vec3d relNewVec = relStartVec.rotateYaw(angle * i);
            Vec3d newVec = startVec.add(relNewVec);

            //Randomizer bag synergy
            if (bagInventory != null) {
                itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
                blockState = BuildModifiers.getBlockStateFromItem(itemStack, player, startPos, EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
            }

            //TODO rotate

            blockStates.add(blockState);
            itemStacks.add(itemStack);
        }

        return blockStates;
    }

    public static boolean isEnabled(RadialMirrorSettings r, BlockPos startPos) {
        if (r == null || !r.enabled) return false;

        //within mirror distance
        if (startPos.getX() + 0.5 < r.position.x - r.radius || startPos.getX() + 0.5 > r.position.x + r.radius ||
            startPos.getY() + 0.5 < r.position.y - r.radius || startPos.getY() + 0.5 > r.position.y + r.radius ||
            startPos.getZ() + 0.5 < r.position.z - r.radius || startPos.getZ() + 0.5 > r.position.z + r.radius)
            return false;

        return true;
    }

}
