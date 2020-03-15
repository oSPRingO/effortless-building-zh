package nl.requios.effortlessbuilding.network;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.BlockSet;
import nl.requios.effortlessbuilding.buildmodifier.UndoRedo;

import java.util.ArrayList;
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

                    PlayerEntity player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);
                    EffortlessBuilding.logTranslate(player, message.prefix, message.translationKey, message.suffix, message.actionBar);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
