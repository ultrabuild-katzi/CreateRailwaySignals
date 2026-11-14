package de.jannik.createrailwaysignal.block;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Matrix4f;

/**
 * Renders a small always-visible icon lying flat on the track at the position a
 * {@code TrackTargetingBehaviour} is bound to - mirrors how Create's own station marks the rail
 * tie it is bound to with a flag/overlay, so trains always show this icon while passing the point.
 */
public final class EdgePointIconRenderer {

    private static final float SIZE = 0.4f;
    private static final float HEIGHT_OFFSET = 0.07f;

    private EdgePointIconRenderer() {}

    public static void render(Identifier texture, World world, BlockPos bePos, BlockPos targetPos,
                               MatrixStack ms, VertexConsumerProvider buffer) {
        ms.push();
        BlockPos offset = targetPos.subtract(bePos);
        ms.translate(offset.getX() + 0.5, offset.getY() + HEIGHT_OFFSET, offset.getZ() + 0.5);

        int light = WorldRenderer.getLightmapCoordinates(world, targetPos);
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        Matrix4f pose = ms.peek().getPositionMatrix();

        float h = SIZE / 2f;
        vertex(vb, pose, -h, 0, -h, 0, 0, light);
        vertex(vb, pose, -h, 0, h, 0, 1, light);
        vertex(vb, pose, h, 0, h, 1, 1, light);
        vertex(vb, pose, h, 0, -h, 1, 0, light);

        ms.pop();
    }

    private static void vertex(VertexConsumer vb, Matrix4f pose, float x, float y, float z,
                                float u, float v, int light) {
        vb.vertex(pose, x, y, z)
                .color(1f, 1f, 1f, 1f)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 1, 0)
                .next();
    }
}
