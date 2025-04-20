package de.jannik.createrailwaysignal.item;

import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import de.jannik.createrailwaysignal.graph.CustomEdgePointType;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrackLimitItem extends TrackTargetingBlockItem {
    public TrackLimitItem(Block block, Settings settings) {
        super(block, settings, CustomEdgePointType.SPEED_SIGNAL);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.create_railway_signal.track_limit.tooltip_1"));
        tooltip.add(Text.translatable("item.create_railway_signal.track_limit.tooltip_2"));
    }
}
