package net.mattseq.echoes_of_time.events;

import net.mattseq.echoes_of_time.EchoesOfTime;
import net.mattseq.echoes_of_time.RewindController;
import net.mattseq.echoes_of_time.RewindGlobalManager;
import net.mattseq.echoes_of_time.item.ModItems;
import net.mattseq.echoes_of_time.networking.ModNetworking;
import net.mattseq.echoes_of_time.networking.TotemPacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = EchoesOfTime.MODID)
public class RewindEventHandler {

    private static int oldTotemDamage;

    public static boolean totemRewindStarted = false;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;

        ItemStack totem = findTotemOfEchoes(player);
        ItemStack scepter = findScepterOfEchoes(player);

        RewindController rewindController = RewindGlobalManager.getOrCreateController(player);
        if ((!totem.isEmpty()) || (!scepter.isEmpty())) {
            rewindController.tick(System.currentTimeMillis());
        }

        // for totem damage
        if (totemRewindStarted && !rewindController.isRewinding) {
            totem.setDamageValue(oldTotemDamage + totem.getMaxDamage()/4);
            if (totem.getDamageValue() >= totem.getMaxDamage()) {
                totem.shrink(1);
            }
            totemRewindStarted = false;
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        // Check for Totem of Echoes in either hand
        ItemStack totem = findTotemOfEchoes(player);
        if (!totem.isEmpty()) {
            // Cancel death
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());

            oldTotemDamage = findTotemOfEchoes(player).getDamageValue();

            CriteriaTriggers.USED_TOTEM.trigger(player, Items.TOTEM_OF_UNDYING.getDefaultInstance());
            ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TotemPacket(true));

            // Custom rewind logic
            RewindGlobalManager.getOrCreateController(player).startRewind(1);
            totemRewindStarted = true;
        }
    }

    private static ItemStack findTotemOfEchoes(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (main.getItem() == ModItems.TOTEM_OF_ECHOES.get()) return main;
        if (off.getItem() == ModItems.TOTEM_OF_ECHOES.get()) return off;
        return ItemStack.EMPTY;
    }

    private static ItemStack findScepterOfEchoes(ServerPlayer player) {
        Item scepter = ModItems.SCEPTER_OF_ECHOES.get();

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == scepter) {
                return stack;
            }
        }

        // Also check offhand and armor slots if needed:
        if (player.getOffhandItem().getItem() == scepter) {
            return player.getOffhandItem();
        }
        if (player.getMainHandItem().getItem() == scepter) {
            return player.getMainHandItem();
        }

        return ItemStack.EMPTY; // Not found
    }
}
