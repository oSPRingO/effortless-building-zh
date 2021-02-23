package nl.requios.effortlessbuildingzh;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.requios.effortlessbuildingzh.buildmode.BuildModes;
import nl.requios.effortlessbuildingzh.buildmode.ModeOptions;

import java.util.HashMap;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModClientEventHandler {

    private static final HashMap<BuildModes.BuildModeEnum, ResourceLocation> buildModeIcons = new HashMap<>();
    private static final HashMap<ModeOptions.ActionEnum, ResourceLocation> modeOptionIcons = new HashMap<>();

    @SubscribeEvent
    public static void onTextureStitch(final TextureStitchEvent.Pre event) {
        EffortlessBuildingZh.log("Stitching textures");
        //register icon textures
        for (final BuildModes.BuildModeEnum mode : BuildModes.BuildModeEnum.values())
        {
            final ResourceLocation spriteLocation = new ResourceLocation(EffortlessBuildingZh.MODID, "icons/" + mode.name().toLowerCase());
            event.addSprite(spriteLocation);
            buildModeIcons.put(mode, spriteLocation);
        }

        for (final ModeOptions.ActionEnum action : ModeOptions.ActionEnum.values())
        {
            final ResourceLocation spriteLocation = new ResourceLocation(EffortlessBuildingZh.MODID, "icons/" + action.name().toLowerCase());
            event.addSprite(spriteLocation);
            modeOptionIcons.put(action, spriteLocation);
        }
    }

    public static TextureAtlasSprite getBuildModeIcon(BuildModes.BuildModeEnum mode) {
        return Minecraft.getInstance().getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).getSprite(buildModeIcons.get(mode));
    }

    public static TextureAtlasSprite getModeOptionIcon(ModeOptions.ActionEnum action) {
        return Minecraft.getInstance().getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).getSprite(modeOptionIcons.get(action));
    }
}
