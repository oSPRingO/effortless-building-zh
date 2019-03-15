package nl.requios.effortlessbuilding.render;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmode.IBuildMode;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager.ModeSettings;
import nl.requios.effortlessbuilding.buildmodifier.BuildModifiers;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager.ModifierSettings;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.helper.InventoryHelper;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class BlockPreviewRenderer {
    private static List<BlockPos> previousCoordinates;
    private static List<IBlockState> previousBlockStates;
    private static List<ItemStack> previousItemStacks;
    private static BlockPos previousFirstPos;
    private static BlockPos previousSecondPos;
    private static int soundTime = 0;

    static class PlacedData {
        float time;
        List<BlockPos> coordinates;
        List<IBlockState> blockStates;
        List<ItemStack> itemStacks;
        BlockPos firstPos;
        BlockPos secondPos;
        boolean breaking;

        public PlacedData(float time, List<BlockPos> coordinates, List<IBlockState> blockStates,
                          List<ItemStack> itemStacks, BlockPos firstPos, BlockPos secondPos, boolean breaking) {
            this.time = time;
            this.coordinates = coordinates;
            this.blockStates = blockStates;
            this.itemStacks = itemStacks;
            this.firstPos = firstPos;
            this.secondPos = secondPos;
            this.breaking = breaking;
        }
    }

    private static List<PlacedData> placedDataList = new ArrayList<>();

    private static final int secondaryTextureUnit = 7;

    public static void render(EntityPlayer player, ModifierSettings modifierSettings, ModeSettings modeSettings) {

        //Render placed blocks with dissolve effect
        //Use fancy shader if config allows, otherwise no dissolve
        if (BuildConfig.visuals.useShaders) {
            RenderHandler.beginBlockPreviews();
            for (int i = 0; i < placedDataList.size(); i++) {
                PlacedData placed = placedDataList.get(i);
                if (placed.coordinates != null && !placed.coordinates.isEmpty()) {

                    double totalTime = MathHelper.clampedLerp(30, 60, placed.firstPos.distanceSq(placed.secondPos) / 100.0) * BuildConfig.visuals.dissolveTimeMultiplier;
                    float dissolve = (ClientProxy.ticksInGame - placed.time) / (float) totalTime;
                    renderBlockPreviews(placed.coordinates, placed.blockStates, placed.itemStacks, dissolve, placed.firstPos, placed.secondPos, false, placed.breaking);
                }
            }
            RenderHandler.endBlockPreviews();
        }
        //Expire
        placedDataList.removeIf(placed -> {
            double totalTime = MathHelper.clampedLerp(30, 60, placed.firstPos.distanceSq(placed.secondPos) / 100.0) * BuildConfig.visuals.dissolveTimeMultiplier;
            return placed.time + totalTime < ClientProxy.ticksInGame;
        });

        //Render block previews
        RayTraceResult lookingAt = ClientProxy.getLookingAt(player);
        if (modeSettings.getBuildMode() == BuildModes.BuildModeEnum.NORMAL) lookingAt = Minecraft.getMinecraft().objectMouseOver;

        ItemStack mainhand = player.getHeldItemMainhand();
        boolean toolInHand = !(!mainhand.isEmpty() && CompatHelper.isItemBlockProxy(mainhand));

        BlockPos startPos = null;
        EnumFacing sideHit = null;
        Vec3d hitVec = null;

        //Checking for null is necessary! Even in vanilla when looking down ladders it is occasionally null (instead of Type MISS)
        if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK) {
            startPos = lookingAt.getBlockPos();

            //Check if tool (or none) in hand
            boolean replaceable = player.world.getBlockState(startPos).getBlock().isReplaceable(player.world, startPos);
            boolean becomesDoubleSlab = SurvivalHelper.doesBecomeDoubleSlab(player, startPos, lookingAt.sideHit);
            if (!modifierSettings.doQuickReplace() && !toolInHand && !replaceable && !becomesDoubleSlab) {
                startPos = startPos.offset(lookingAt.sideHit);
            }

            //Get under tall grass and other replaceable blocks
            if (modifierSettings.doQuickReplace() && !toolInHand && replaceable) {
                startPos = startPos.down();
            }

            sideHit = lookingAt.sideHit;
            hitVec = lookingAt.hitVec;
        }

        //Dont render if in normal mode and modifiers are disabled
        //Unless alwaysShowBlockPreview is true in config
        if (doRenderBlockPreviews(modifierSettings, modeSettings, startPos)) {

            IBuildMode buildModeInstance = modeSettings.getBuildMode().instance;
            if (buildModeInstance.getSideHit(player) != null) sideHit = buildModeInstance.getSideHit(player);
            if (buildModeInstance.getHitVec(player) != null) hitVec = buildModeInstance.getHitVec(player);

            if (sideHit != null) {

                //Should be red?
                boolean breaking = BuildModes.currentlyBreakingClient.get(player) != null && BuildModes.currentlyBreakingClient.get(player);

                //get coordinates
                List<BlockPos> startCoordinates = BuildModes.findCoordinates(player, startPos, breaking || modifierSettings.doQuickReplace());

                BlockPos firstPos = BlockPos.ORIGIN, secondPos = BlockPos.ORIGIN;
                //Remember first and last pos for the shader
                if (!startCoordinates.isEmpty()) {
                    firstPos = startCoordinates.get(0);
                    secondPos = startCoordinates.get(startCoordinates.size() - 1);
                }

                //Limit number of blocks you can place
                int limit = ReachHelper.getMaxBlocksPlacedAtOnce(player);
                if (startCoordinates.size() > limit) {
                    startCoordinates = startCoordinates.subList(0, limit);
                }

                List<BlockPos> newCoordinates = BuildModifiers.findCoordinates(player, startCoordinates);

                sortOnDistanceToPlayer(newCoordinates, player);

                hitVec = new Vec3d(Math.abs(hitVec.x - ((int) hitVec.x)), Math.abs(hitVec.y - ((int) hitVec.y)),
                        Math.abs(hitVec.z - ((int) hitVec.z)));

                //Get blockstates
                List<ItemStack> itemStacks = new ArrayList<>();
                List<IBlockState> blockStates = new ArrayList<>();
                if (breaking) {
                    //Find blockstate of world
                    for (BlockPos coordinate : newCoordinates) {
                        blockStates.add(player.world.getBlockState(coordinate));
                    }
                } else {
                    blockStates = BuildModifiers.findBlockStates(player, startCoordinates, hitVec, sideHit, itemStacks);
                }


                //Check if they are different from previous
                //TODO fix triggering when moving player
                if (!BuildModifiers.compareCoordinates(previousCoordinates, newCoordinates)) {
                    previousCoordinates = newCoordinates;
                    //remember the rest for placed blocks
                    previousBlockStates = blockStates;
                    previousItemStacks = itemStacks;
                    previousFirstPos = firstPos;
                    previousSecondPos = secondPos;

                    //if so, renew randomness of randomizer bag
                    ItemRandomizerBag.renewRandomness();
                    //and play sound (max once every tick)
                    if (startCoordinates.size() > 1 && blockStates.size() > 1 && soundTime < ClientProxy.ticksInGame - 0) {
                        soundTime = ClientProxy.ticksInGame;

                        SoundType soundType = blockStates.get(0).getBlock().getSoundType(blockStates.get(0), player.world,
                                newCoordinates.get(0), player);
                        player.world.playSound(player, player.getPosition(), breaking ? soundType.getBreakSound() : soundType.getPlaceSound(),
                                SoundCategory.BLOCKS, 0.3f, 0.8f);
                    }
                }

                //Render block previews
                if (blockStates.size() != 0 && newCoordinates.size() == blockStates.size()) {
                    int blockCount;

                    //Use fancy shader if config allows, otherwise outlines
                    if (BuildConfig.visuals.useShaders && newCoordinates.size() < BuildConfig.visuals.shaderTreshold) {

                        RenderHandler.beginBlockPreviews();

                        blockCount = renderBlockPreviews(newCoordinates, blockStates, itemStacks, 0f, firstPos, secondPos, !breaking, breaking);

                        RenderHandler.endBlockPreviews();
                    } else {

                        RenderHandler.beginLines();

                        Vec3d color = new Vec3d(1f, 1f, 1f);
                        if (breaking) color = new Vec3d(1f, 0f, 0f);

                        for (int i = newCoordinates.size() - 1; i >= 0; i--) {
                            AxisAlignedBB boundingBox = blockStates.get(i).getBoundingBox(player.world, newCoordinates.get(i));
                            RenderHandler.renderBlockOutline(newCoordinates.get(i), boundingBox, color);
                        }

                        RenderHandler.endLines();

                        blockCount = newCoordinates.size();
                    }

                    //Display block count and dimensions in actionbar
                    if (BuildModes.isActive(player)) {
                        BlockPos dim = secondPos.subtract(firstPos);
                        dim = new BlockPos(Math.abs(dim.getX()) + 1, Math.abs(dim.getY()) + 1, Math.abs(dim.getZ()) + 1);

                        String dimensions = "(";
                        if (dim.getX() > 1) dimensions += dim.getX() + "x";
                        if (dim.getZ() > 1) dimensions += dim.getZ() + "x";
                        if (dim.getY() > 1) dimensions += dim.getY() + "x";
                        dimensions = dimensions.substring(0, dimensions.length() - 1);
                        if (dimensions.length() > 1) dimensions += ")";

                        EffortlessBuilding.log(player, blockCount + " blocks " + dimensions, true);
                    }
                }


            }

            RenderHandler.beginLines();
            //Draw outlines if tool in hand
            //Find proper raytrace: either normal range or increased range depending on canBreakFar
            RayTraceResult objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
            RayTraceResult breakingRaytrace = ReachHelper.canBreakFar(player) ? lookingAt : objectMouseOver;
            if (toolInHand && breakingRaytrace != null && breakingRaytrace.typeOfHit == RayTraceResult.Type.BLOCK) {
                List<BlockPos> breakCoordinates = BuildModifiers.findCoordinates(player, breakingRaytrace.getBlockPos());

                //Only render first outline if further than normal reach
                boolean excludeFirst = objectMouseOver != null && objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK;
                for (int i = excludeFirst ? 1 : 0; i < breakCoordinates.size(); i++) {
                    BlockPos coordinate = breakCoordinates.get(i);

                    IBlockState blockState = player.world.getBlockState(coordinate);
                    if (!blockState.getBlock().isAir(blockState, player.world, coordinate)) {
                        if (SurvivalHelper.canBreak(player.world, player, coordinate) || i == 0) {
                            AxisAlignedBB boundingBox = blockState.getBoundingBox(player.world, coordinate);
                            RenderHandler.renderBlockOutline(coordinate, boundingBox, new Vec3d(0f, 0f, 0f));
                        }
                    }
                }
            }
            RenderHandler.endLines();
        }
    }

    //Whether to draw any block previews or outlines
    public static boolean doRenderBlockPreviews(ModifierSettings modifierSettings, ModeSettings modeSettings, BlockPos startPos) {
        return modeSettings.getBuildMode() != BuildModes.BuildModeEnum.NORMAL ||
               (startPos != null && BuildModifiers.isEnabled(modifierSettings, startPos)) ||
               BuildConfig.visuals.alwaysShowBlockPreview;
    }

    protected static int renderBlockPreviews(List<BlockPos> coordinates, List<IBlockState> blockStates,
                                              List<ItemStack> itemStacks, float dissolve, BlockPos firstPos,
                                              BlockPos secondPos, boolean checkCanPlace, boolean red) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        int blocksValid = 0;

        if (coordinates.isEmpty()) return blocksValid;

        for (int i = coordinates.size() - 1; i >= 0; i--) {
            BlockPos blockPos = coordinates.get(i);
            IBlockState blockState = blockStates.get(i);
            ItemStack itemstack = itemStacks.isEmpty() ? ItemStack.EMPTY : itemStacks.get(i);
            if (CompatHelper.isItemBlockProxy(itemstack))
                itemstack = CompatHelper.getItemBlockByState(itemstack, blockState);

            //Check if can place
            //If check is turned off, check if blockstate is the same (for dissolve effect)
            if ((!checkCanPlace /*&& player.world.getBlockState(blockPos) == blockState*/) || //TODO enable
                SurvivalHelper.canPlace(player.world, player, blockPos, blockState, itemstack, modifierSettings.doQuickReplace(), EnumFacing.UP)) {

                ShaderHandler.useShader(ShaderHandler.dissolve, generateShaderCallback(dissolve,
                        new Vec3d(blockPos), new Vec3d(firstPos), new Vec3d(secondPos),
                        blockPos == secondPos, red));
                RenderHandler.renderBlockPreview(dispatcher, blockPos, blockState);
                blocksValid++;
            }
        }
        return blocksValid;
    }

    public static void onBlocksPlaced() {
        onBlocksPlaced(previousCoordinates, previousItemStacks, previousBlockStates, previousFirstPos, previousSecondPos);
    }

    public static void onBlocksPlaced(List<BlockPos> coordinates, List<ItemStack> itemStacks, List<IBlockState> blockStates,
                                      BlockPos firstPos, BlockPos secondPos) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);

        //Check if block previews are enabled
        if (doRenderBlockPreviews(modifierSettings, modeSettings, firstPos)) {

            //Save current coordinates, blockstates and itemstacks
            if (!coordinates.isEmpty() && blockStates.size() == coordinates.size() &&
                coordinates.size() > 1 && coordinates.size() < BuildConfig.visuals.shaderTreshold) {

                placedDataList.add(new PlacedData(ClientProxy.ticksInGame, coordinates, blockStates,
                        itemStacks, firstPos, secondPos, false));
            }
        }

    }

    public static void onBlocksBroken() {
        onBlocksBroken(previousCoordinates, previousItemStacks, previousBlockStates, previousFirstPos, previousSecondPos);
    }

    public static void onBlocksBroken(List<BlockPos> coordinates, List<ItemStack> itemStacks, List<IBlockState> blockStates,
                                      BlockPos firstPos, BlockPos secondPos) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);

        //Check if block previews are enabled
        if (doRenderBlockPreviews(modifierSettings, modeSettings, firstPos)) {

            //Save current coordinates, blockstates and itemstacks
            if (!coordinates.isEmpty() && blockStates.size() == coordinates.size() &&
                coordinates.size() > 1 && coordinates.size() < BuildConfig.visuals.shaderTreshold) {

                sortOnDistanceToPlayer(coordinates, player);

                placedDataList.add(new PlacedData(ClientProxy.ticksInGame, coordinates, blockStates,
                        itemStacks, firstPos, secondPos, true));
            }
        }

    }

    private static Consumer<Integer> generateShaderCallback(final float dissolve, final Vec3d blockpos,
                                                            final Vec3d firstpos, final Vec3d secondpos,
                                                            final boolean highlight, final boolean red) {
        Minecraft mc = Minecraft.getMinecraft();
        return (Integer shader) -> {
            int percentileUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "dissolve");
            int highlightUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "highlight");
            int redUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "red");
            int blockposUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "blockpos");
            int firstposUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "firstpos");
            int secondposUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "secondpos");
            int imageUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "image");
            int maskUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "mask");

            //image
            OpenGlHelper.setActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).getGlTextureId());
            ARBShaderObjects.glUniform1iARB(imageUniform, 0);

            OpenGlHelper.setActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB + secondaryTextureUnit);

            GlStateManager.enableTexture2D();
            GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

            //mask
            GL11.glBindTexture(GL11.GL_TEXTURE_2D,
                    mc.renderEngine.getTexture(new ResourceLocation(EffortlessBuilding.MODID, "textures/shader_mask.png")).getGlTextureId());
            ARBShaderObjects.glUniform1iARB(maskUniform, secondaryTextureUnit);

            //blockpos
            ARBShaderObjects.glUniform3fARB(blockposUniform, (float) blockpos.x, (float) blockpos.y, (float) blockpos.z);
            ARBShaderObjects.glUniform3fARB(firstposUniform, (float) firstpos.x, (float) firstpos.y, (float) firstpos.z);
            ARBShaderObjects.glUniform3fARB(secondposUniform, (float) secondpos.x, (float) secondpos.y, (float) secondpos.z);

            //dissolve
            ARBShaderObjects.glUniform1fARB(percentileUniform, dissolve);
            //highlight
            ARBShaderObjects.glUniform1iARB(highlightUniform, highlight ? 1 : 0);
            //red
            ARBShaderObjects.glUniform1iARB(redUniform, red ? 1 : 0);
        };
    }

    private static void sortOnDistanceToPlayer(List<BlockPos> coordinates, EntityPlayer player) {

        Collections.sort(coordinates, (lhs, rhs) -> {
            // -1 - less than, 1 - greater than, 0 - equal
            double lhsDistanceToPlayer = new Vec3d(lhs).subtract(player.getPositionEyes(1f)).lengthSquared();
            double rhsDistanceToPlayer = new Vec3d(rhs).subtract(player.getPositionEyes(1f)).lengthSquared();
            return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
        });

    }
}
