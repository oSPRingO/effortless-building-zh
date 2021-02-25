package nl.requios.effortlessbuildingzh.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;

@OnlyIn(Dist.CLIENT)
public class RandomizerBagScreen extends ContainerScreen<RandomizerBagContainer> {
    private static final ResourceLocation guiTextures =
            new ResourceLocation(EffortlessBuildingZh.MODID, "textures/gui/container/randomizerbag.png");

    public RandomizerBagScreen(RandomizerBagContainer randomizerBagContainer, PlayerInventory playerInventory, ITextComponent title) {
        super(randomizerBagContainer, playerInventory, new TranslationTextComponent("effortlessbuildingzh.screen.randomizer_bag"));
        ySize = 134;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(this.title.getFormattedText(), 8, 6, 0x404040);
        font.drawString(playerInventory.getDisplayName().getFormattedText(), 8, ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(guiTextures);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        blit(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }
}
