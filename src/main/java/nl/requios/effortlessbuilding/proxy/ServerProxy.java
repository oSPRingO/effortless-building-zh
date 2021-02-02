package nl.requios.effortlessbuilding.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import nl.requios.effortlessbuilding.network.PacketHandler;
import nl.requios.effortlessbuilding.network.TranslatedLogMessage;

import java.util.function.Supplier;

public class ServerProxy implements IProxy {
	//Only physical server! Singleplayer server is seen as clientproxy
	@Override
	public void setup(FMLCommonSetupEvent event) {

	}

	@Override
	public void clientSetup(FMLClientSetupEvent event) {
	}

	public PlayerEntity getPlayerEntityFromContext(Supplier<NetworkEvent.Context> ctx) {
		return ctx.get().getSender();
	}

	@Override
	public void logTranslate(PlayerEntity player, String prefix, String translationKey, String suffix, boolean actionBar) {
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new TranslatedLogMessage(prefix, translationKey, suffix, actionBar));
	}
}
