package net.mattseq.timemachine.events;

import net.mattseq.timemachine.TimeMachine;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TimeMachine.MODID)
public class ClientEvents {
    public static boolean lockMovement = false;
    private static float lockedYaw = 0;
    private static float lockedPitch = 0;

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

//    @SubscribeEvent
//    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase != TickEvent.Phase.END) return;
//
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.player == null) return;
//
//        if (lockMovement) {
//            // Lock view by resetting to stored values
//            mc.player.setYRot(lockedYaw);
//            mc.player.setXRot(lockedPitch);
//        } else {
//            // Store current view when unlocked
//            lockedYaw = mc.player.getYRot();
//            lockedPitch = mc.player.getXRot();
//        }
//    }
}
