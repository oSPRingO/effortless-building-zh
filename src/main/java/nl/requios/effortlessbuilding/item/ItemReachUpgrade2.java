package nl.requios.effortlessbuilding.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemReachUpgrade2 extends Item {

    public ItemReachUpgrade2() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1));
        this.setRegistryName(EffortlessBuilding.MODID, "reach_upgrade2");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (player.isCreative()) {
            if (world.isRemote) EffortlessBuilding.log(player, "Reach upgrades are not necessary in creative.");
            if (world.isRemote) EffortlessBuilding.log(player, "Still want increased reach? Use the config.");
            return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
        }

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        int currentLevel = modifierSettings.getReachUpgrade();
        if (currentLevel == 1) {
            modifierSettings.setReachUpgrade(2);
            if (world.isRemote) EffortlessBuilding.log(player, "Upgraded reach to " + ReachHelper.getMaxReach(player));
            player.setHeldItem(hand, ItemStack.EMPTY);

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("entity.player.levelup"));
            player.playSound(soundEvent, 1f, 1f);
        } else if (currentLevel < 1) {
            if (world.isRemote) EffortlessBuilding.log(player, "Use Reach Upgrade 1 first.");

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("item.armor.equip_leather"));
            player.playSound(soundEvent, 1f, 1f);
        } else if (currentLevel > 1) {
            if (world.isRemote) EffortlessBuilding.log(player, "Already used this upgrade! Current reach is " + ReachHelper
                    .getMaxReach(player) + ".");

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("item.armor.equip_leather"));
            player.playSound(soundEvent, 1f, 1f);
        }
        return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Consume to increase reach to " + TextFormatting.BLUE + BuildConfig.reach.maxReachLevel2.get()));
        tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Previous upgrades need to be consumed first"));
    }

    @Override
    public String getTranslationKey() {
        return this.getRegistryName().toString();
    }
}
