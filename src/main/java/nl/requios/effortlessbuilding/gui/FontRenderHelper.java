package nl.requios.effortlessbuilding.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class FontRenderHelper {
	/*
	private static final FontRenderer font = Minecraft.getInstance().fontRenderer;

	public static void drawSplitString(MatrixStack ms, String str, int x, int y, int wrapWidth, int textColor) {
		str = trimStringNewline(str);
		renderSplitString(ms, str, x, y, wrapWidth, textColor);
	}

	private static void renderSplitString(MatrixStack ms, String str, int x, int y, int wrapWidth, int textColor) {
		List<String> list = font.listFormattedStringToWidth(str, wrapWidth);
		Matrix4f matrix4f = TransformationMatrix.identity().getMatrix();

		for(String s : list) {
			float f = (float)x;
			if (font.getBidiFlag()) {
				int i = font.getStringWidth(font.bidiReorder(s));
				f += (float)(wrapWidth - i);
			}

			font.renderString(s, f, (float)y, textColor, ms.getLast().getMatrix(), false, false);
			y += 9;
		}

	}

	private static String trimStringNewline(String text) {
		while(text != null && text.endsWith("\n")) {
			text = text.substring(0, text.length() - 1);
		}

		return text;
	}

	public static List<String> listFormattedStringToWidth(String str, int wrapWidth) {
		return Arrays.asList(wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
	}

	public static String wrapFormattedStringToWidth(String str, int wrapWidth) {
		String s;
		String s1;
		for(s = ""; !str.isEmpty(); s = s + s1 + "\n") {
			int i = font.sizeStringToWidth(str, wrapWidth);
			if (str.length() <= i) {
				return s + str;
			}

			s1 = str.substring(0, i);
			char c0 = str.charAt(i);
			boolean flag = c0 == ' ' || c0 == '\n';
			str = TextFormatting.getFormatString(s1) + str.substring(i + (flag ? 1 : 0));
		}

		return s;
	}*/
}
