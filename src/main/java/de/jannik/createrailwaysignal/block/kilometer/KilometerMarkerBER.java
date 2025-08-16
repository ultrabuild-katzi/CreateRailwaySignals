package de.jannik.createrailwaysignal.block.kilometer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class KilometerMarkerBER implements BlockEntityRenderer<KilometerMarkerBlockEntity> {
    private static final float TEXT_SCALE = 0.055f;  // text size
    private static final float Z_OFFSET   = -0.43f;

    private final TextRenderer text;

    public KilometerMarkerBER(BlockEntityRendererFactory.Context ctx) {
        this.text = ctx.getTextRenderer();
    }

    @Override
    public void render(KilometerMarkerBlockEntity be, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vcp, int light, int overlay) {
        if (be.getWorld() == null) return;

        Direction facing = be.getCachedState().get(Properties.HORIZONTAL_FACING);

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);

        switch (facing) {
            case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            case WEST  -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            case EAST  -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
            default -> {}
        }

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

        matrices.scale(TEXT_SCALE, -TEXT_SCALE, 1.0f);

        matrices.translate(0, 0, Z_OFFSET);

        String top    = Integer.toString(be.getKilometer());
        String bottom = Integer.toString(be.getMeters() / 100);

        int topWidth    = text.getWidth(top);
        int bottomWidth = text.getWidth(bottom);

        int yTop = -7;
        int yBottom = 1;

        text.draw(top, -topWidth / 2f, yTop, 0x000000, false,
                matrices.peek().getPositionMatrix(), vcp,
                TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);

        text.draw(bottom, -bottomWidth / 2f, yBottom, 0x000000, false,
                matrices.peek().getPositionMatrix(), vcp,
                TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);

        matrices.pop();
    }
}
