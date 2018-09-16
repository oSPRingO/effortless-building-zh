package nl.requios.effortlessbuilding.inventory;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;

@SideOnly(Side.CLIENT)
public class RandomizerBagGuiContainer extends GuiContainer {
    private static final ResourceLocation grinderGuiTextures =
            new ResourceLocation(EffortlessBuilding.MODID
                    + ":textures/gui/container/randomizerbag.png");
    private final InventoryPlayer inventoryPlayer;
    private final IItemHandler inventoryBag;

    public RandomizerBagGuiContainer(InventoryPlayer parInventoryPlayer,
                                     IItemHandler parInventoryBag) {
        super(new RandomizerBagContainer(parInventoryPlayer,
                parInventoryBag));
        inventoryPlayer = parInventoryPlayer;
        inventoryBag = parInventoryBag;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = "Randomizer bag";
        fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        fontRenderer.drawString(inventoryPlayer.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    /**
     * Args : renderPartialTicks, mouseX, mouseY
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                   int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(grinderGuiTextures);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }
}
