package de.jannik.createrailwaysignal.block;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BueSchrankBlockRenderer extends GeoBlockRenderer<BueSchrankBlockEntity> {
    public BueSchrankBlockRenderer(BlockEntityRendererFactory.Context context) {
        super(new BueSchrankModel());
    }
}
