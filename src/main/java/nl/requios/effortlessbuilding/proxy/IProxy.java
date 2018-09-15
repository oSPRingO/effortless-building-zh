package nl.requios.effortlessbuilding.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface IProxy
{
    /**
     * Register entities and networking.
     */
    void preInit(FMLPreInitializationEvent event);

    /**
     * Register event listeners, recipes and advancements.
     */
    void init(FMLInitializationEvent event);

    /**
     * For doing inter-mod stuff like checking which mods are loaded or if you want a complete view of things across
     * mods like having a list of all registered items to aid random item generation.
     */
    void postInit(FMLPostInitializationEvent event);

    /**
     * Server commands should be registered here.
     */
    void serverStarting(FMLServerStartingEvent event);

    /**
     * Returns a side-appropriate EntityPlayer for use during message handling.
     *
     * @param parContext the context
     * @return the player entity from context
     */
    EntityPlayer getPlayerEntityFromContext(MessageContext parContext);
}
