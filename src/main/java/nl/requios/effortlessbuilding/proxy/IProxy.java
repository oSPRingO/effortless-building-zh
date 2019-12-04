package nl.requios.effortlessbuilding.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface IProxy {
    void setup(final FMLCommonSetupEvent event);

    void clientSetup(final FMLClientSetupEvent event);

    EntityPlayer getPlayerEntityFromContext(Supplier<NetworkEvent.Context> ctx);
}
