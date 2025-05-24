package net.mattseq.echoes_of_time.events;

import net.mattseq.echoes_of_time.EchoesOfTime;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EchoesOfTime.MODID)
public class ClientEvents {
    public static boolean lockMovement = false;

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (lockMovement) {
            // Stop movement by setting impulses to 0
            event.getInput().forwardImpulse = 0;
            event.getInput().leftImpulse = 0;
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
        }
    }
}
