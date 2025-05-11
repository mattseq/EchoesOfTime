package net.mattseq.timemachine.events;

import net.mattseq.timemachine.RewindGlobalManager;
import net.mattseq.timemachine.TimeMachine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TimeMachine.MODID)
public class PlayerTickEvent {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;

        RewindGlobalManager.getOrCreateController(player).tick(System.currentTimeMillis());
    }
}
