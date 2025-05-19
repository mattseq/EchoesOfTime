package net.mattseq.timemachine.events;

import net.mattseq.timemachine.RewindController;
import net.mattseq.timemachine.RewindGlobalManager;
import net.mattseq.timemachine.TimeMachine;
import net.mattseq.timemachine.item.ModItems;
import net.mattseq.timemachine.networking.ModNetworking;
import net.mattseq.timemachine.networking.TotemPacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = TimeMachine.MODID)
public class TotemEventHandler {

    private static int oldTotemDamage;

    public static boolean rewindStarted = false;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        ItemStack totem = findTotemOfEchoes(player);
        RewindController rewindController = RewindGlobalManager.getOrCreateController(player);
        if (!totem.isEmpty()) {
            rewindController.tick(System.currentTimeMillis());
        }
        if (rewindStarted && !rewindController.isRewinding) {
            totem.setDamageValue(oldTotemDamage + totem.getMaxDamage()/4);
            if (totem.getDamageValue() >= totem.getMaxDamage()) {
                totem.shrink(1);
            }
            rewindStarted = false;
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

            oldTotemDamage = findTotemOfEchoes(player).getDamageValue();

            CriteriaTriggers.USED_TOTEM.trigger(player, Items.TOTEM_OF_UNDYING.getDefaultInstance());
            ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TotemPacket(true));

            // Custom rewind logic
            RewindGlobalManager.getOrCreateController(player).startRewind(1);
            rewindStarted = true;
        }
    }

    private static ItemStack findTotemOfEchoes(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (main.getItem() == ModItems.TOTEM_OF_ECHOES.get()) return main;
        if (off.getItem() == ModItems.TOTEM_OF_ECHOES.get()) return off;
        return ItemStack.EMPTY;
    }
}
