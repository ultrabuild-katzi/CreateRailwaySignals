package de.jannik.createrailwaysignal.block;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.simibubi.create.content.redstone.nixieTube.DoubleFaceAttachedBlock.DoubleAttachFace;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlock;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.utility.*;

import io.github.fabricators_of_create.porting_lib.util.FontRenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.text.Style;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.random.Random;

public class LightSignalSpeedBlockRenderer extends SafeBlockEntityRenderer<LightSignalSpeedBlockEntity> {

    private static Random r = Random.create();


    public LightSignalSpeedBlockRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(LightSignalSpeedBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer,
                              int light, int overlay) {
        ms.push();
        BlockState blockState = be.getCachedState();
        DoubleAttachFace face = DoubleAttachFace.FLOOR;
        float yRot = AngleHelper.horizontalAngle(blockState.get(NixieTubeBlock.FACING));
        float xRot = AngleHelper.verticalAngle(blockState.get(NixieTubeBlock.FACING));

        TransformStack msr = TransformStack.cast(ms);
        msr.centre()
                .rotateY(yRot)
                .rotateZ(xRot)
                .unCentre();

        msr.centre();

        float height = 7;
        float scale = 1 / 21f;

        String s = be.getDisplayedStrings();
        Couple<Integer> colorCouple;
        if(!be.getCachedState().get(LightSignalSpeedBlock.JEB_MODE)) {
             colorCouple = DyeHelper.DYE_TABLE.get(be.getCachedState().get(LightSignalSpeedBlock.DYE_COLOR));
        } else {
            int ticks = (int) be.getWorld().getTime();
            int m = 25;
            int ordinal = ticks / m;
            int dyeColors = DyeColor.values().length;
            int currentIndex = ordinal % dyeColors;
            int newIndex = (ordinal + 1) % dyeColors;
            float transition = ((float)(ticks % m)) / m;
            float[] fs = SheepEntity.getRgbColor(DyeColor.byId(currentIndex
            ));
            float[] gs = SheepEntity.getRgbColor(DyeColor.byId(newIndex));
            float r = fs[0] * (1.0F - transition) + gs[0] * transition;
            float g = fs[1] * (1.0F - transition) + gs[1] * transition;
            float b = fs[2] * (1.0F - transition) + gs[2] * transition;

            var color = new java.awt.Color(r, g, b);

            colorCouple = Couple.create(color.brighter().getRGB(), color.getRGB());
        }


        ms.push();
        ms.translate(-0.03, 0, -0.1);
        ms.scale(scale, -scale, scale);
        drawShadowText(ms, buffer, s, height, colorCouple);
        ms.pop();

        ms.pop();
    }

    public static void drawShadowText(MatrixStack ms, VertexConsumerProvider buffer, String c, float height, Couple<Integer> color) {
        TextRenderer fontRenderer = MinecraftClient.getInstance().textRenderer;
        float charWidth = fontRenderer.getWidth(c);
        float shadowOffset = .5f;
        float flicker = r.nextFloat();
        int brightColor = color.getFirst();
        int darkColor = color.getSecond();
        int flickeringBrightColor = com.simibubi.create.foundation.utility.Color.mixColors(brightColor, darkColor, flicker / 4);

        ms.push();
        ms.translate((charWidth - shadowOffset) / -2f, -height, 1);
        drawInWorldString(ms, buffer, c, flickeringBrightColor);
        ms.push();
        ms.translate(shadowOffset, shadowOffset, -1 / 16f);
        drawInWorldString(ms, buffer, c, darkColor);
        ms.pop();
        ms.pop();

        ms.push();
        ms.scale(-1, 1, 1);
        ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
        drawInWorldString(ms, buffer, c, darkColor);
        ms.push();
        ms.translate(-shadowOffset, shadowOffset, -1 / 16f);
        drawInWorldString(ms, buffer, c, Color.mixColors(darkColor, 0, .35f));
        ms.pop();
        ms.pop();
    }

    public static void drawInWorldString(MatrixStack ms, VertexConsumerProvider buffer, String c, int color) {
        TextRenderer fontRenderer = MinecraftClient.getInstance().textRenderer;
        fontRenderer.draw(c, 0, 0, color, false, ms.peek()
                .getPositionMatrix(), buffer, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        if (buffer instanceof VertexConsumerProvider.Immediate) {
            GlyphRenderer texturedglyph = FontRenderUtil.getFontStorage(fontRenderer, Style.DEFAULT_FONT_ID)
                    .getRectangleRenderer();
            ((VertexConsumerProvider.Immediate) buffer).draw(texturedglyph.getLayer(TextRenderer.TextLayerType.NORMAL));
        }
    }

    @Override
    public int getRenderDistance() {
        return 128;
    }


}