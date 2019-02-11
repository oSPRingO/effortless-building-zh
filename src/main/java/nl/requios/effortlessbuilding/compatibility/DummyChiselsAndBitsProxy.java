package nl.requios.effortlessbuilding.compatibility;

import net.minecraft.util.EnumHand;

public class DummyChiselsAndBitsProxy implements IChiselsAndBitsProxy {
    @Override
    public boolean isHoldingChiselTool(EnumHand hand) {
        return false;
    }
}
