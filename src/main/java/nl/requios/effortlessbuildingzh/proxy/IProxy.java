package nl.requios.effortlessbuildingzh.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface IProxy {
    void setup(final FMLCommonSetupEvent event);

    void clientSetup(final FMLClientSetupEvent event);

    PlayerEntity getPlayerEntityFromContext(Supplier<NetworkEvent.Context> ctx);

    void logTranslate(PlayerEntity player, String prefix, String translationKey, String suffix, boolean actionBar);
}
