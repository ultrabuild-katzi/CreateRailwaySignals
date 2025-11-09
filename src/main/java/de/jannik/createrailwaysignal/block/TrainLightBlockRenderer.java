package de.jannik.createrailwaysignal.block;

import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class TrainLightBlockRenderer extends SmartBlockEntityRenderer<TrainLightBlockEntity> {

    public TrainLightBlockRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(TrainLightBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        // Get current color from BlockEntity - use direct field access to avoid caching issues
        TrainLightBlock.LightColor currentColor = be.currentColor;

        // Debug output to see if renderer is even called
        if (be.getWorld() != null && be.getWorld().getTime() % 100 == 0) {
//            System.out.println("[TrainLight RENDERER] Rendering with color: " + currentColor);
        }

        // Render a glowing overlay based on the current color
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getTranslucent());
        Matrix4f pose = ms.peek().getPositionMatrix();

        // Set color based on the BlockEntity's current color
        float r, g, b;
        if (currentColor == TrainLightBlock.LightColor.RED) {
            r = 1.0f;
            g = 0.0f;
            b = 0.0f;
        } else { // WHITE
            r = 1.0f;
            g = 1.0f;
            b = 1.0f;
        }

        // Render a small glowing cube in the center to show the color
        float size = 0.3f;
        float offset = 0.5f - size / 2;
        renderGlowingCube(pose, vb,
            offset, offset, offset,
            offset + size, offset + size, offset + size,
            r, g, b, 0.8f, 0xF000F0); // Full brightness for glow effect
    }

    private void renderGlowingCube(Matrix4f pose, VertexConsumer vb, float x0, float y0, float z0,
                                    float x1, float y1, float z1, float r, float g, float b, float a, int light) {
        // Front face (z+)
        vb.vertex(pose, x0, y0, z1).color(r, g, b, a).texture(0, 0).light(light).normal(0, 0, 1).next();
        vb.vertex(pose, x1, y0, z1).color(r, g, b, a).texture(1, 0).light(light).normal(0, 0, 1).next();
        vb.vertex(pose, x1, y1, z1).color(r, g, b, a).texture(1, 1).light(light).normal(0, 0, 1).next();
        vb.vertex(pose, x0, y1, z1).color(r, g, b, a).texture(0, 1).light(light).normal(0, 0, 1).next();

        // Back face (z-)
        vb.vertex(pose, x1, y0, z0).color(r, g, b, a).texture(0, 0).light(light).normal(0, 0, -1).next();
        vb.vertex(pose, x0, y0, z0).color(r, g, b, a).texture(1, 0).light(light).normal(0, 0, -1).next();
        vb.vertex(pose, x0, y1, z0).color(r, g, b, a).texture(1, 1).light(light).normal(0, 0, -1).next();
        vb.vertex(pose, x1, y1, z0).color(r, g, b, a).texture(0, 1).light(light).normal(0, 0, -1).next();

        // Left face (x-)
        vb.vertex(pose, x0, y0, z0).color(r, g, b, a).texture(0, 0).light(light).normal(-1, 0, 0).next();
        vb.vertex(pose, x0, y0, z1).color(r, g, b, a).texture(1, 0).light(light).normal(-1, 0, 0).next();
        vb.vertex(pose, x0, y1, z1).color(r, g, b, a).texture(1, 1).light(light).normal(-1, 0, 0).next();
        vb.vertex(pose, x0, y1, z0).color(r, g, b, a).texture(0, 1).light(light).normal(-1, 0, 0).next();

        // Right face (x+)
        vb.vertex(pose, x1, y0, z1).color(r, g, b, a).texture(0, 0).light(light).normal(1, 0, 0).next();
        vb.vertex(pose, x1, y0, z0).color(r, g, b, a).texture(1, 0).light(light).normal(1, 0, 0).next();
        vb.vertex(pose, x1, y1, z0).color(r, g, b, a).texture(1, 1).light(light).normal(1, 0, 0).next();
        vb.vertex(pose, x1, y1, z1).color(r, g, b, a).texture(0, 1).light(light).normal(1, 0, 0).next();

        // Top face (y+)
        vb.vertex(pose, x0, y1, z1).color(r, g, b, a).texture(0, 0).light(light).normal(0, 1, 0).next();
        vb.vertex(pose, x1, y1, z1).color(r, g, b, a).texture(1, 0).light(light).normal(0, 1, 0).next();
        vb.vertex(pose, x1, y1, z0).color(r, g, b, a).texture(1, 1).light(light).normal(0, 1, 0).next();
        vb.vertex(pose, x0, y1, z0).color(r, g, b, a).texture(0, 1).light(light).normal(0, 1, 0).next();

        // Bottom face (y-)
        vb.vertex(pose, x0, y0, z0).color(r, g, b, a).texture(0, 0).light(light).normal(0, -1, 0).next();
        vb.vertex(pose, x1, y0, z0).color(r, g, b, a).texture(1, 0).light(light).normal(0, -1, 0).next();
        vb.vertex(pose, x1, y0, z1).color(r, g, b, a).texture(1, 1).light(light).normal(0, -1, 0).next();
        vb.vertex(pose, x0, y0, z1).color(r, g, b, a).texture(0, 1).light(light).normal(0, -1, 0).next();
    }
}
