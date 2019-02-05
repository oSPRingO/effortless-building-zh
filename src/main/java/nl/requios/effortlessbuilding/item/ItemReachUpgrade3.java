package nl.requios.effortlessbuilding.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemReachUpgrade3 extends Item {

    public ItemReachUpgrade3() {
        this.setRegistryName(EffortlessBuilding.MODID, "reach_upgrade3");
        this.setUnlocalizedName(this.getRegistryName().toString());

        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.isCreative()) {
            if (world.isRemote) EffortlessBuilding.log(player, "Reach upgrades are not necessary in creative.");
            if (world.isRemote) EffortlessBuilding.log(player, "Still want increased reach? Use the config.");
            return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
        }

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        int currentLevel = modifierSettings.getReachUpgrade();
        if (currentLevel == 2) {
            modifierSettings.setReachUpgrade(3);
            if (world.isRemote) EffortlessBuilding.log(player, "Upgraded reach to " + ReachHelper.getMaxReach(player));
            player.setHeldItem(hand, ItemStack.EMPTY);

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("entity.player.levelup"));
            player.playSound(soundEvent, 1f, 1f);
        } else if (currentLevel < 2) {
            if (currentLevel == 0)
                if (world.isRemote) EffortlessBuilding.log(player, "Use Reach Upgrade 1 and 2 first.");
            if (currentLevel == 1)
                if (world.isRemote) EffortlessBuilding.log(player, "Use Reach Upgrade 2 first.");

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("item.armor.equip_leather"));
            player.playSound(soundEvent, 1f, 1f);
        } else if (currentLevel > 2) {
            if (world.isRemote) EffortlessBuilding.log(player, "Already used this upgrade! Current reach is " + ReachHelper
                    .getMaxReach(player) + ".");

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("item.armor.equip_leather"));
            player.playSound(soundEvent, 1f, 1f);
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Consume to increase reach to " + TextFormatting.BLUE + BuildConfig.reach.maxReachLevel3);
        tooltip.add(TextFormatting.GRAY + "Previous upgrades need to be consumed first");
    }

    @Override
    public String getUnlocalizedName() {
        return super.getUnlocalizedName();
    }
}
