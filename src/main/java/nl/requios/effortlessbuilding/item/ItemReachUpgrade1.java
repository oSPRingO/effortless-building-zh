package nl.requios.effortlessbuilding.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemReachUpgrade1 extends Item {

    public ItemReachUpgrade1() {
        this.setRegistryName(EffortlessBuilding.MODID, "reach_upgrade1");
        this.setUnlocalizedName(this.getRegistryName().toString());

        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
        int currentLevel = buildSettings.getReachUpgrade();
        if (currentLevel == 0) {
            buildSettings.setReachUpgrade(1);

            if (world.isRemote) EffortlessBuilding.log(player, "Upgraded reach to level " + ReachHelper.getMaxReach(player));
            player.setHeldItem(hand, ItemStack.EMPTY);

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("entity.player.levelup"));
            player.playSound(soundEvent, 1f, 1f);
        } else if (currentLevel > 0) {
            if (world.isRemote) EffortlessBuilding.log(player, "Already used this upgrade! Current reach is " + ReachHelper
                    .getMaxReach(player) + ".");

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("item.armor.equip_leather"));
            player.playSound(soundEvent, 1f, 1f);
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        EffortlessBuilding.log("used");
        return ItemStack.EMPTY;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Consume to increase reach to " + TextFormatting.BLUE + BuildConfig.reach.maxReachLevel1);
    }

    @Override
    public String getUnlocalizedName() {
        return super.getUnlocalizedName();
    }
}
