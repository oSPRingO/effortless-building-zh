package nl.requios.effortlessbuilding.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.inventory.RandomizerBagCapabilityProvider;

import javax.annotation.Nullable;

public class ItemRandomizerBag extends Item {

    public ItemRandomizerBag(){
        this.setRegistryName(EffortlessBuilding.MODID, "randomizer_bag");
        this.setUnlocalizedName(this.getRegistryName().toString());

        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (worldIn.isRemote) return EnumActionResult.PASS;
        EffortlessBuilding.log(player, "onItemUse");

        if (player.isSneaking()){
            //Open inventory
            player.openGui(EffortlessBuilding.instance, EffortlessBuilding.RANDOMIZER_BAG_GUI, worldIn, 0, 0, 0);
        } else {
            //Place block

        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (worldIn.isRemote) return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
        EffortlessBuilding.log(playerIn, "onItemRightClick");

        //Open inventory
        playerIn.openGui(EffortlessBuilding.instance, EffortlessBuilding.RANDOMIZER_BAG_GUI, worldIn, 0, 0, 0);

        return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new RandomizerBagCapabilityProvider();
    }
}
