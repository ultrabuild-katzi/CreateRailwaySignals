package de.jannik.createrailwaysignal.item;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item INCOMPLETE_LIGHT_SIGNAL_SPEED = registerItem(
            "incomplete_light_signal_speed", new SequencedAssemblyItem(new Item.Settings()));

    public static final Item VISITOR_ID = registerItem(
            "visitor_id", new IDItem(new Item.Settings()));

    public static final Item SUPERVISOR_ID = registerItem(
            "supervisor_id", new IDItem(new Item.Settings()));

    public static final Item WORKER_ID = registerItem(
            "worker_id", new IDItem(new Item.Settings()));



    private static <T extends Item> T registerItem(String name, T item) {
        return Registry.register(Registries.ITEM, Identifier.of(Createrailwaysignal.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Createrailwaysignal.LOGGER.info("Registering Mod Items for " + Createrailwaysignal.MOD_ID);
    }




}