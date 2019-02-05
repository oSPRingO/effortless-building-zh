package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;

@SideOnly(Side.CLIENT)
public class RandomizerBagGuiContainer extends GuiContainer {
    private static final ResourceLocation guiTextures =
            new ResourceLocation(EffortlessBuilding.MODID, "textures/gui/container/randomizerbag.png");
    private final InventoryPlayer inventoryPlayer;
    private final IItemHandler inventoryBag;

    public RandomizerBagGuiContainer(InventoryPlayer inventoryPlayer, IItemHandler inventoryBag) {
        super(new RandomizerBagContainer(inventoryPlayer, inventoryBag));
        this.inventoryPlayer = inventoryPlayer;
        this.inventoryBag = inventoryBag;

        ySize = 134;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = "Randomizer Bag";
        fontRenderer.drawString(s, 8, 6, 0x404040);
        fontRenderer.drawString(inventoryPlayer.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(guiTextures);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }
}
