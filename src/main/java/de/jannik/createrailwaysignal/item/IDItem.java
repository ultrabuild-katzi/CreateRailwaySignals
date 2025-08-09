package de.jannik.createrailwaysignal.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class IDItem extends Item {
    public static final Integer STACK_SIZE = 1;
    private static final String SIGNED_KEY = "SignedBy";

    public IDItem(Settings settings) {
        super(settings.maxCount(STACK_SIZE));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt() && Objects.requireNonNull(stack.getNbt()).contains(SIGNED_KEY)) {
            String signedBy = stack.getNbt().getString(SIGNED_KEY);
            tooltip.add(Text.literal("Signed by " + signedBy).formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        } else {
            tooltip.add(Text.translatable("item.createrailwaysignal.visitor_id.tooltip").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user.isSneaking()) {
            NbtCompound nbt = stack.getOrCreateNbt();

            if (!nbt.contains(SIGNED_KEY)) {
                String playerName = user.getName().getString();
                nbt.putString(SIGNED_KEY, playerName);
                user.sendMessage(Text.literal("You signed this ID."), false);
            } else {
                user.sendMessage(Text.literal("This ID is already signed."), false);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }
}