package nl.requios.effortlessbuilding.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.network.ModifierSettingsMessage;

public class CommandReach extends CommandBase {
    @Override
    public String getName() {
        return "reach";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.reach.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = (EntityPlayerMP) sender;
        if (args.length != 1) {
            int reachUpgrade = ModifierSettingsManager.getModifierSettings(player).getReachUpgrade();
            EffortlessBuilding.log(player, "Current reach: level "+reachUpgrade);
            throw new WrongUsageException("commands.reach.usage");
        }

        if (sender instanceof EntityPlayerMP) {
            //Set reach level to args[0]
            ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
            modifierSettings.setReachUpgrade(Integer.valueOf(args[0]));
            ModifierSettingsManager.setModifierSettings(player, modifierSettings);
            //Send to client
            EffortlessBuilding.packetHandler.sendTo(new ModifierSettingsMessage(modifierSettings), player);

            sender.sendMessage(new TextComponentString("Reach level of " + sender.getName() + " set to " + modifierSettings
                    .getReachUpgrade()));
        }
    }
}
