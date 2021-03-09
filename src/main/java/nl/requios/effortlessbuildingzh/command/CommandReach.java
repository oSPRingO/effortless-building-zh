package nl.requios.effortlessbuildingzh.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuildingzh.network.ModifierSettingsMessage;
import nl.requios.effortlessbuildingzh.network.PacketHandler;

public class CommandReach {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("reach").then(Commands.literal("set").then(Commands.argument("level", IntegerArgumentType.integer(0, 3)).executes((context) -> {
            return setReachLevel(context.getSource().asPlayer(), IntegerArgumentType.getInteger(context, "level"));
        }))).then(Commands.literal("get").executes((context -> {
            return getReachLevel(context.getSource().asPlayer());
        }))));
    }

    private static int setReachLevel(ServerPlayerEntity player, int level){
        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        modifierSettings.setReachUpgrade(level);
        ModifierSettingsManager.setModifierSettings(player, modifierSettings);
        //Send to client
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ModifierSettingsMessage(modifierSettings));

        player.sendMessage(new StringTextComponent("已将" + player.getName().getString() + "的可及区设为" + modifierSettings.getReachUpgrade() + "级"));

        return 1;
    }

    private static int getReachLevel(ServerPlayerEntity player){
        int reachUpgrade = ModifierSettingsManager.getModifierSettings(player).getReachUpgrade();
        EffortlessBuildingZh.log(player, "当前可及区为" + reachUpgrade + "级");

        return 1;
    }
}
