package nl.requios.effortlessbuilding;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

@SideOnly(Side.CLIENT)
public class RenderHelper {
    public static void begin(float partialTicks) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        Vec3d playerPos = new Vec3d(playerX, playerY, playerZ);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTranslated(-playerPos.x, -playerPos.y, -playerPos.z);

        GL11.glLineWidth(2);
        GL11.glDepthMask(false);
    }

    public static void end() {
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static void renderBlockOutline(BlockPos pos) {
        renderBlockOutline(pos, pos);
    }

    //Renders outline. Pos1 has to be minimal x,y,z and pos2 maximal x,y,z
    public static void renderBlockOutline(BlockPos pos1, BlockPos pos2) {
        GL11.glLineWidth(2);

        AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2.add(1, 1, 1)).grow(0.0020000000949949026);

        RenderGlobal.drawSelectionBoundingBox(aabb, 1f, 1f, 1f, 0.6f);
    }

}
