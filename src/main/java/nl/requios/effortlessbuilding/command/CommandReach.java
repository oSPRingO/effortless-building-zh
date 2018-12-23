package nl.requios.effortlessbuilding.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.network.BuildSettingsMessage;

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
        if (args.length != 1) {
            throw new WrongUsageException("commands.reach.usage");
        }

        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            //Set reach level to args[0]
            BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
            buildSettings.setReachUpgrade(Integer.valueOf(args[0]));
            BuildSettingsManager.setBuildSettings(player, buildSettings);
            //Send to client
            EffortlessBuilding.packetHandler.sendTo(new BuildSettingsMessage(buildSettings), player);

            sender.sendMessage(new TextComponentString("Reach level of " + sender.getName() + " set to " + buildSettings.getReachUpgrade()));
        }
    }
}
