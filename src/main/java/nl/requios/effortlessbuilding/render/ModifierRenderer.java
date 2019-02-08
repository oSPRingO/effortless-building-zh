package nl.requios.effortlessbuilding.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nl.requios.effortlessbuilding.buildmodifier.Mirror;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.RadialMirror;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

@SideOnly(Side.CLIENT)
public class ModifierRenderer {

    protected static final Color colorX = new Color(255, 72, 52);
    protected static final Color colorY = new Color(67, 204, 51);
    protected static final Color colorZ = new Color(52, 247, 255);
    protected static final Color colorRadial = new Color(52, 247, 255);
    protected static final int lineAlpha = 200;
    protected static final int planeAlpha = 75;
    protected static final Vec3d epsilon = new Vec3d(0.001, 0.001, 0.001); //prevents z-fighting

    public static void render(ModifierSettingsManager.ModifierSettings modifierSettings) {
        RenderHandler.beginLines();

        //Mirror lines and areas
        Mirror.MirrorSettings m = modifierSettings.getMirrorSettings();
        if (m != null && m.enabled && (m.mirrorX || m.mirrorY || m.mirrorZ))
        {
            Vec3d pos = m.position.add(epsilon);
            int radius = m.radius;

            if (m.mirrorX)
            {
                Vec3d posA = new Vec3d(pos.x, pos.y - radius, pos.z - radius);
                Vec3d posB = new Vec3d(pos.x, pos.y + radius, pos.z + radius);

                drawMirrorPlane(posA, posB, colorX, m.drawLines, m.drawPlanes, true);
            }
            if (m.mirrorY)
            {
                Vec3d posA = new Vec3d(pos.x - radius, pos.y, pos.z - radius);
                Vec3d posB = new Vec3d(pos.x + radius, pos.y, pos.z + radius);

                drawMirrorPlaneY(posA, posB, colorY, m.drawLines, m.drawPlanes);
            }
            if (m.mirrorZ)
            {
                Vec3d posA = new Vec3d(pos.x - radius, pos.y - radius, pos.z);
                Vec3d posB = new Vec3d(pos.x + radius, pos.y + radius, pos.z);

                drawMirrorPlane(posA, posB, colorZ, m.drawLines, m.drawPlanes, true);
            }

            //Draw axis coordinated colors if two or more axes are enabled
            //(If only one is enabled the lines are that planes color)
            if (m.drawLines && ((m.mirrorX && m.mirrorY) || (m.mirrorX && m.mirrorZ) || (m.mirrorY && m.mirrorZ)))
            {
                drawMirrorLines(m);
            }
        }

        //Radial mirror lines and areas
        RadialMirror.RadialMirrorSettings r = modifierSettings.getRadialMirrorSettings();
        if (r != null && r.enabled)
        {
            Vec3d pos = r.position.add(epsilon);
            int radius = r.radius;

            float angle = 2f * ((float) Math.PI) / r.slices;
            Vec3d relStartVec = new Vec3d(radius, 0, 0);
            if (r.slices%4 == 2) relStartVec = relStartVec.rotateYaw(angle / 2f);

            for (int i = 0; i < r.slices; i++) {
                Vec3d relNewVec = relStartVec.rotateYaw(angle * i);
                Vec3d newVec = pos.add(relNewVec);

                Vec3d posA = new Vec3d(pos.x, pos.y - radius, pos.z);
                Vec3d posB = new Vec3d(newVec.x, pos.y + radius, newVec.z);
                drawMirrorPlane(posA, posB, colorRadial, r.drawLines, r.drawPlanes, false);
            }
        }

        RenderHandler.endLines();
    }


    //----Mirror----

    protected static void drawMirrorPlane(Vec3d posA, Vec3d posB, Color c, boolean drawLines, boolean drawPlanes, boolean drawVerticalLines) {

        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        if (drawPlanes) {
            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posA.x, posB.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posB.x, posA.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posB.x, posB.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();

            tessellator.draw();
        }

        if (drawLines) {
            Vec3d middle = posA.add(posB).scale(0.5);
            bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            bufferBuilder.pos(posA.x, middle.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(posB.x, middle.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            if (drawVerticalLines) {
                bufferBuilder.pos(middle.x, posA.y, middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
                bufferBuilder.pos(middle.x, posB.y, middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            }

            tessellator.draw();
        }
    }

    protected static void drawMirrorPlaneY(Vec3d posA, Vec3d posB, Color c, boolean drawLines, boolean drawPlanes) {

        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        if (drawPlanes) {
            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posA.x, posA.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posB.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posB.x, posA.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();

            tessellator.draw();
        }

        if (drawLines) {
            Vec3d middle = posA.add(posB).scale(0.5);
            bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            bufferBuilder.pos(middle.x, middle.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(middle.x, middle.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(posA.x, middle.y, middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(posB.x, middle.y, middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();

            tessellator.draw();
        }
    }

    protected static void drawMirrorLines(Mirror.MirrorSettings m) {

        Vec3d pos = m.position.add(epsilon);

        GL11.glColor4d(100, 100, 100, 255);
        GL11.glLineWidth(2);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        bufferBuilder.pos(pos.x - m.radius, pos.y, pos.z).color(colorX.getRed(), colorX.getGreen(), colorX.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x + m.radius, pos.y, pos.z).color(colorX.getRed(), colorX.getGreen(), colorX.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y - m.radius, pos.z).color(colorY.getRed(), colorY.getGreen(), colorY.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y + m.radius, pos.z).color(colorY.getRed(), colorY.getGreen(), colorY.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y, pos.z - m.radius).color(colorZ.getRed(), colorZ.getGreen(), colorZ.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y, pos.z + m.radius).color(colorZ.getRed(), colorZ.getGreen(), colorZ.getBlue(), lineAlpha).endVertex();

        tessellator.draw();
    }
}
