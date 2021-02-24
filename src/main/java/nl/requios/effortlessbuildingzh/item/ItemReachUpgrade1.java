package nl.requios.effortlessbuildingzh.item;

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
import nl.requios.effortlessbuildingzh.BuildConfig;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuildingzh.helper.ReachHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemReachUpgrade1 extends Item {

    public ItemReachUpgrade1() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1));
        this.setRegistryName(EffortlessBuildingZh.MODID, "reach_upgrade1");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (player.isCreative()) {
            if (world.isRemote) EffortlessBuildingZh.log(player, "手长等级在创造模式下不可用");
            if (world.isRemote) EffortlessBuildingZh.log(player, "仍想提升手长？请转到配置文件。");
            return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
        }

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        int currentLevel = modifierSettings.getReachUpgrade();
        if (currentLevel == 0) {
            modifierSettings.setReachUpgrade(1);

            if (world.isRemote) EffortlessBuildingZh.log(player, "已将手长提升至" + ReachHelper.getMaxReach(player) + "级");
            player.setHeldItem(hand, ItemStack.EMPTY);

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("entity.player.levelup"));
            player.playSound(soundEvent, 1f, 1f);
        } else if (currentLevel > 0) {
            if (world.isRemote) EffortlessBuildingZh.log(player, "已经升到这一级了！当前的手长等级是" + ReachHelper
                    .getMaxReach(player) + ".");

            SoundEvent soundEvent = new SoundEvent(new ResourceLocation("item.armor.equip_leather"));
            player.playSound(soundEvent, 1f, 1f);
        }
        return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(TextFormatting.GRAY + "可将手长提升至" + TextFormatting.BLUE + BuildConfig.reach.maxReachLevel1.get() + TextFormatting.GRAY + "方块"));
    }

    @Override
    public String getTranslationKey() {
        return this.getRegistryName().toString();
    }
}
