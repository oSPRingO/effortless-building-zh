package nl.requios.effortlessbuilding.compatibility;

import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ChiselToolType;
import net.minecraft.util.EnumHand;

public class ActiveChiselsAndBitsProxy implements IChiselsAndBitsProxy {
    @Override
    public boolean isHoldingChiselTool(EnumHand hand) {
        ChiselToolType toolType = ClientSide.instance.getHeldToolType(hand);
        return toolType != null && toolType.hasMenu();
    }
}
