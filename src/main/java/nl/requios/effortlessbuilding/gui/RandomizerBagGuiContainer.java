package nl.requios.effortlessbuilding.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;

@OnlyIn(Dist.CLIENT)
public class RandomizerBagGuiContainer extends ContainerScreen {
    private static final ResourceLocation guiTextures =
            new ResourceLocation(EffortlessBuilding.MODID, "textures/gui/container/randomizerbag.png");
    private final PlayerInventory inventoryPlayer;
    private final IItemHandler inventoryBag;

    public RandomizerBagGuiContainer(PlayerInventory inventoryPlayer, IItemHandler inventoryBag) {
        super(new RandomizerBagContainer(inventoryPlayer, inventoryBag));
        this.inventoryPlayer = inventoryPlayer;
        this.inventoryBag = inventoryBag;

        ySize = 134;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = "Randomizer Bag";
        font.drawString(s, 8, 6, 0x404040);
        font.drawString(inventoryPlayer.getDisplayName().getUnformattedComponentText(), 8, ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(guiTextures);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }
}
