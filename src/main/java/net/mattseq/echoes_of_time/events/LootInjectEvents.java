package net.mattseq.echoes_of_time.events;

import net.mattseq.echoes_of_time.EchoesOfTime;
import net.mattseq.echoes_of_time.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EchoesOfTime.MODID)
public class LootInjectEvents {
    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        ResourceLocation lootTable = event.getName();

        if (lootTable.equals(BuiltInLootTables.ANCIENT_CITY)) {
            LootPool pool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))              // Number of times to roll this pool
                    .add(LootItem.lootTableItem(ModItems.FRACTURED_HOURGLASS.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.1f)))      // chance to drop
                    .build();

            event.getTable().addPool(pool);
        }

        if (event.getName().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "entities/warden"))) {
            LootPool temporalCorePool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))              // Number of times to roll this pool
                    .add(LootItem.lootTableItem(ModItems.TEMPORAL_CORE.get())
                            .when(LootItemRandomChanceCondition.randomChance(.3f)))      // chance to drop
                    .build();
            event.getTable().addPool(temporalCorePool);

            LootPool wardenRibsPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(8))              // Number of times to roll this pool
                    .add(LootItem.lootTableItem(ModItems.WARDEN_RIB.get())
                            .when(LootItemRandomChanceCondition.randomChance(.375f)))      // chance to drop
                    .build();
            event.getTable().addPool(wardenRibsPool);
        }
    }
}
