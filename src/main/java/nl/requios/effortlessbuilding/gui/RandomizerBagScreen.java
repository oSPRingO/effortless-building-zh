package nl.requios.effortlessbuilding.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuilding;

@OnlyIn(Dist.CLIENT)
public class RandomizerBagScreen extends ContainerScreen<RandomizerBagContainer> {
	private static final ResourceLocation guiTextures =
		new ResourceLocation(EffortlessBuilding.MODID, "textures/gui/container/randomizerbag.png");

	public RandomizerBagScreen(RandomizerBagContainer randomizerBagContainer, PlayerInventory playerInventory, ITextComponent title) {
		super(randomizerBagContainer, playerInventory, title);//new TranslationTextComponent("effortlessbuilding.screen.randomizer_bag"));
		ySize = 134;
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		renderBackground(ms);
		super.render(ms, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(ms, mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack ms, int mouseX, int mouseY) {
		font.func_243246_a(ms, this.title, 8, 6, 0x404040);
		font.func_243246_a(ms, playerInventory.getDisplayName(), 8, ySize - 96 + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack ms, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(guiTextures);
		int marginHorizontal = (width - xSize) / 2;
		int marginVertical = (height - ySize) / 2;
		blit(ms, marginHorizontal, marginVertical, 0, 0, xSize, ySize);
	}
}
