package nl.requios.effortlessbuildingzh.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;

import java.util.function.Supplier;

public class TranslatedLogMessage {
    private String prefix;
    private String translationKey;
    private String suffix;
    private boolean actionBar;

    public TranslatedLogMessage(){
        prefix = "";
        translationKey = "";
        suffix = "";
        actionBar = false;
    }

    public TranslatedLogMessage(String prefix, String translationKey, String suffix, boolean actionBar) {
        this.prefix = prefix;
        this.translationKey = translationKey;
        this.suffix = suffix;
        this.actionBar = actionBar;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getSuffix() {
        return suffix;
    }

    public boolean isActionBar() {
        return actionBar;
    }

    public static void encode(TranslatedLogMessage message, PacketBuffer buf) {
        buf.writeString(message.prefix);
        buf.writeString(message.translationKey);
        buf.writeString(message.suffix);
        buf.writeBoolean(message.actionBar);
    }

    public static TranslatedLogMessage decode(PacketBuffer buf) {
        return new TranslatedLogMessage(buf.readString(), buf.readString(), buf.readString(), buf.readBoolean());
    }

    public static class Handler
    {
        public static void handle(TranslatedLogMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                    //Received clientside

                    PlayerEntity player = EffortlessBuildingZh.proxy.getPlayerEntityFromContext(ctx);
                    EffortlessBuildingZh.logTranslate(player, message.prefix, message.translationKey, message.suffix, message.actionBar);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
