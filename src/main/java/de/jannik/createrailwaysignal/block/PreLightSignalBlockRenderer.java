package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.graph.PreLightSignalBoundary;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Renders the prelight signal's speedometer icon flat on the track, at the position its edge
 * point is actually bound to - always visible, the same way Create marks a station's working
 * position.
 */
public class PreLightSignalBlockRenderer extends SafeBlockEntityRenderer<PreLightSignalBlockEntity> {

    private static final Identifier ICON = new Identifier(Createrailwaysignal.MOD_ID, "textures/block/speedometer.png");

    public PreLightSignalBlockRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(PreLightSignalBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer,
                               int light, int overlay) {
        if (be.getWorld() == null)
            return;

        TrackTargetingBehaviour<PreLightSignalBoundary> edgePoint = be.edgePoint;
        if (edgePoint == null)
            return;

        BlockPos targetPosition = edgePoint.getGlobalPosition();
        if (!(be.getWorld().getBlockState(targetPosition).getBlock() instanceof ITrackBlock))
            return;

        EdgePointIconRenderer.render(ICON, be.getWorld(), be.getPos(), targetPosition, ms, buffer);
    }

    @Override
    public boolean rendersOutsideBoundingBox(PreLightSignalBlockEntity be) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 128;
    }
}
