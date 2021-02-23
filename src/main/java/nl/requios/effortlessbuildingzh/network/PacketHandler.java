package nl.requios.effortlessbuildingzh.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(EffortlessBuildingZh.MODID, "main_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        int id = 0;

        INSTANCE.registerMessage(id++, ModifierSettingsMessage.class, ModifierSettingsMessage::encode, ModifierSettingsMessage::decode, ModifierSettingsMessage.Handler::handle);
        INSTANCE.registerMessage(id++, ModeSettingsMessage.class, ModeSettingsMessage::encode, ModeSettingsMessage::decode, ModeSettingsMessage.Handler::handle);
        INSTANCE.registerMessage(id++, ModeActionMessage.class, ModeActionMessage::encode, ModeActionMessage::decode, ModeActionMessage.Handler::handle);
        INSTANCE.registerMessage(id++, BlockPlacedMessage.class, BlockPlacedMessage::encode, BlockPlacedMessage::decode, BlockPlacedMessage.Handler::handle);
        INSTANCE.registerMessage(id++, BlockBrokenMessage.class, BlockBrokenMessage::encode, BlockBrokenMessage::decode, BlockBrokenMessage.Handler::handle);
        INSTANCE.registerMessage(id++, CancelModeMessage.class, CancelModeMessage::encode, CancelModeMessage::decode, CancelModeMessage.Handler::handle);
        INSTANCE.registerMessage(id++, RequestLookAtMessage.class, RequestLookAtMessage::encode, RequestLookAtMessage::decode, RequestLookAtMessage.Handler::handle);
        INSTANCE.registerMessage(id++, AddUndoMessage.class, AddUndoMessage::encode, AddUndoMessage::decode, AddUndoMessage.Handler::handle);
        INSTANCE.registerMessage(id++, ClearUndoMessage.class, ClearUndoMessage::encode, ClearUndoMessage::decode, ClearUndoMessage.Handler::handle);

    }

}
