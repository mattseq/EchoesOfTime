package net.mattseq.timemachine.item;

import net.mattseq.timemachine.TimeMachine;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TimeMachine.MODID);

    public static final RegistryObject<Item> TOTEM_OF_ECHOES = ITEMS.register("totem_of_echoes",
            () -> new TotemOfEchoesItem(new Item.Properties()));

    public static final RegistryObject<Item> SCEPTER_OF_ECHOES = ITEMS.register("scepter_of_echoes",
            () -> new ScepterOfEchoesItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
