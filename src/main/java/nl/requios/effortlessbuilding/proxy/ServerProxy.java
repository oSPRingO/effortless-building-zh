package nl.requios.effortlessbuilding.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerProxy implements IProxy {
    //Only physical server! Singleplayer server is seen as clientproxy
    @Override
    public void setup(FMLCommonSetupEvent event) {

    }

    @Override
    public void clientSetup(FMLClientSetupEvent event) {}

    public EntityPlayer getPlayerEntityFromContext(Supplier<NetworkEvent.Context> ctx){
        return ctx.get().getSender();
    }
}
