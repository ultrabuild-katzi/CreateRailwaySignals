package de.jannik.createrailwaysignal.block;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.block.ModBlocks;

public class BueSchrankModel extends GeoModel<BueSchrankBlockEntity> {
    @Override
    public Identifier getModelResource(BueSchrankBlockEntity animatable) {
        String name = "bue_schrank_2m";
        if (animatable.getCachedState().isOf(ModBlocks.BUE_SCHRANK_3M.get())) {
            name = "bue_schrank_3m";
        } else if (animatable.getCachedState().isOf(ModBlocks.BUE_SCHRANK_4M.get())) {
            name = "bue_schrank_4m";
        }
        return new Identifier(Createrailwaysignal.MOD_ID, "geo/blocks/" + name + ".geo.json");
    }

    @Override
    public Identifier getTextureResource(BueSchrankBlockEntity animatable) {
        return new Identifier(Createrailwaysignal.MOD_ID, "textures/block/bue_schrank.png");
    }

    @Override
    public Identifier getAnimationResource(BueSchrankBlockEntity animatable) {
        String name = "bue_schrank_2m";
        if (animatable.getCachedState().isOf(ModBlocks.BUE_SCHRANK_3M.get())) {
            name = "bue_schrank_3m";
        } else if (animatable.getCachedState().isOf(ModBlocks.BUE_SCHRANK_4M.get())) {
            name = "bue_schrank_4m";
        }
        return new Identifier(Createrailwaysignal.MOD_ID, "animations/" + name + ".animation.json");
    }
}
