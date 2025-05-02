package de.jannik.createrailwaysignal.item;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.utility.Lang;
import de.jannik.createrailwaysignal.block.LightSignalSpeedBlockEntity;
import de.jannik.createrailwaysignal.block.ModBlockEntityTypes;
import de.jannik.createrailwaysignal.block.TrackLimitBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LightSignalSpeedItem extends BlockItem {


    public static BlockPos currentSelection;
    public static ItemStack currentItem;

    public LightSignalSpeedItem(Block block, Settings settings) {
        super(block, settings);

    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.create_railway_signal.light_signal_speed.tooltip_1"));
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            ItemStack itemStack = player.getStackInHand(hand);
            NbtCompound nbtCompound = getBlockEntityNbt(itemStack);
            player.playSound(AllSoundEvents.FWOOMP.getMainEvent(), 1, 1);

            if (nbtCompound != null) {
                currentSelection = null;
                currentItem = null;
                nbtCompound.remove("targetBlock");
                return TypedActionResult.success(itemStack);
            }


            return TypedActionResult.fail(itemStack);
        }

        return super.use(world, player, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || !player.isSneaking() && context.getHand() == Hand.MAIN_HAND)
            return super.useOnBlock(context);

        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        if (!(state.getBlock() instanceof TrackLimitBlock))
            return super.useOnBlock(context);

        ItemStack itemStack = context.getStack();
        NbtCompound nbtCompound = getBlockEntityNbt(itemStack);
        if (nbtCompound == null)
            nbtCompound = new NbtCompound();

        LightSignalSpeedBlockEntity.writeTargetBlock(nbtCompound, context.getBlockPos());
        setBlockEntityNbt(itemStack, ModBlockEntityTypes.TRACK_LIMIT.get(), nbtCompound);
        currentSelection = context.getBlockPos();


        String key = "weighted_ejector.target_set";
        player.sendMessage(Lang.translateDirect(key)
                .formatted(Formatting.GOLD), true);
        player.playSound(AllSoundEvents.CONFIRM.getMainEvent(), 1, 1);

        return ActionResult.SUCCESS;
    }
}
