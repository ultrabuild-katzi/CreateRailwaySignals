package de.jannik.createrailwaysignal.block;


import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.utility.DyeHelper;
import io.github.fabricators_of_create.porting_lib.util.FontRenderUtil;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.text.Style;

public class BrSignBlockRenderer extends SafeBlockEntityRenderer<BrSignBlockEntity> {

    @SuppressWarnings("unused")
    public BrSignBlockRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(BrSignBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        ms.push();

        var blockState = be.getCachedState();

        Direction facing = blockState.get(BrSignBlock.FACING);
        String s = be.getDisplayedString();

        // base scale converts font pixels to world units (kept similar to LightSignalSpeed)
        float baseScale = 1 / 21f;
        // Interpret 'width' as percent relative to default (100 = no change). This makes user adjustments visible.
        float scale = baseScale * ((float) be.getWidth() / 100f);

        float height = 6f;

        // apply a small translation to position text on the face and apply depth (push back for positive depth)
        // translate BEFORE scaling (matches LightSignalSpeedBlockRenderer behaviour)
        float depthOffset = be.getDepth() * 0.02f;
        float initialZ = -0.1f - depthOffset;
        ms.translate(-0.03, 0, initialZ);

        // apply scaling with Y inverted (matches LightSignalSpeed rendering) so text appears upright
        ms.scale(scale, -scale, scale);

        Couple<Integer> colorCouple;
        if (!blockState.get(BrSignBlock.JEB_MODE)) {
            var perSign = be.getTextColor();
            if (perSign != null) {
                colorCouple = DyeHelper.getDyeColors(perSign);
            } else {
                colorCouple = DyeHelper.getDyeColors(blockState.get(BrSignBlock.DYE_COLOR));
            }
        } else {
            int ticks = (int) (be.getWorld() != null ? be.getWorld().getTime() : 0L);
            int m = 25;
            int ordinal = ticks / m;
            int dyeColors = DyeColor.values().length;
            int currentIndex = ordinal % dyeColors;
            int newIndex = (ordinal + 1) % dyeColors;
            float transition = ((float) (ticks % m)) / m;
            float[] fs = SheepEntity.getRgbColor(DyeColor.byId(currentIndex));
            float[] gs = SheepEntity.getRgbColor(DyeColor.byId(newIndex));
            float r = fs[0] * (1.0F - transition) + gs[0] * transition;
            float g = fs[1] * (1.0F - transition) + gs[1] * transition;
            float b = fs[2] * (1.0F - transition) + gs[2] * transition;

            var color = new java.awt.Color(r, g, b);
            colorCouple = Couple.create(color.brighter().getRGB(), color.getRGB());
        }

        // Offset für Text anwenden
        ms.translate(be.getHorizontalOffset() * scale, be.getVerticalOffset() * scale, 0);
        // draw text ohne Flicker
        drawShadowText(ms, buffer, s, height, colorCouple, facing);

        ms.pop();
    }

    private static void drawShadowText(MatrixStack ms, VertexConsumerProvider buffer, String c, float height, Couple<Integer> color, Direction facing) {
        TextRenderer fontRenderer = MinecraftClient.getInstance().textRenderer;
        float charWidth = fontRenderer.getWidth(c);
        float shadowOffset = .5f;
        int brightColor = color.getFirst();
        int darkColor = color.getSecond();
        float yRot = switch (facing) {
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };

        ms.push();
        if (yRot != 0f) {
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRot));
        }
        ms.translate((charWidth - shadowOffset) / -2f, -height, 1);
        drawInWorldString(ms, buffer, c, brightColor);
        ms.push();
        ms.translate(shadowOffset, shadowOffset, -1 / 16f);
        drawInWorldString(ms, buffer, c, darkColor);
        ms.pop();
        ms.pop();

        ms.push();
        ms.scale(-1, 1, 1);
        if (yRot != 0f) {
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRot));
        }
        ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
        drawInWorldString(ms, buffer, c, darkColor);
        ms.push();
        ms.translate(-shadowOffset, shadowOffset, -1 / 16f);
        drawInWorldString(ms, buffer, c, Color.mixColors(darkColor, 0, .35f));
        ms.pop();
        ms.pop();
    }

    private static void drawInWorldString(MatrixStack ms, VertexConsumerProvider buffer, String c, int color) {
        TextRenderer fontRenderer = MinecraftClient.getInstance().textRenderer;
        fontRenderer.draw(c, 0, 0, color, false, ms.peek()
                .getPositionMatrix(), buffer, TextRenderer.TextLayerType.NORMAL, 0, 0x00F000F0);
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
