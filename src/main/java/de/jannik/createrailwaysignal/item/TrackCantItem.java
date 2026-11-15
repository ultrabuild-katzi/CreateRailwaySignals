package de.jannik.createrailwaysignal.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.World;

import java.util.List;

/**
 * Right-click a curved track piece with this item to open a GUI that sets the track's cant
 * (Überhöhung/banking) on that curve, either manually (in mm) or automatically from a nearby
 * {@link de.jannik.createrailwaysignal.block.TrackLimitBlock} speed limit and the curve's radius.
 * <p>
 * All the interaction logic lives client-side in {@code CreaterailwaysignalClient}, since
 * detecting which curve point is targeted relies on Create's client-only curve selection.
 */
public class TrackCantItem extends Item {
    public TrackCantItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.createrailwaysignal.track_cant.tooltip_1"));
        tooltip.add(Text.translatable("item.createrailwaysignal.track_cant.tooltip_embankment"));
        tooltip.add(Text.translatable("item.createrailwaysignal.track_cant.tooltip_embankment_remove"));
    }
}
