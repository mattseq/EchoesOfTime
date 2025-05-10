package net.mattseq.timemachine;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewindGlobalManager {
    private static final Map<UUID, RewindController> controllers = new HashMap<>();

    public static RewindController getOrCreateController(ServerPlayer player) {
        return controllers.computeIfAbsent(player.getUUID(), uuid -> new RewindController(player));
    }

    public static void removeController(ServerPlayer player) {
        controllers.remove(player.getUUID());
    }

    public static void tickAll(long currentTimeMillis) {
        for (RewindController controller : controllers.values()) {
            controller.tick(currentTimeMillis);
        }
    }
}