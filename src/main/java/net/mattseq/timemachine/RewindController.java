package net.mattseq.timemachine;

import net.mattseq.timemachine.events.ClientEvents;
import net.mattseq.timemachine.snapshots.WorldSnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.Deque;

public class RewindController {
    private static final long MAX_SNAPSHOT_AGE_MS = 10000; // Store 10 seconds of snapshots
    private static final long SNAPSHOT_INTERVAL_MS = 500;  // Take a snapshot every 0.5 seconds

    private final ServerPlayer player;
    private final Deque<CompoundTag> rewindBuffer = new ArrayDeque<>();

    private boolean isRewinding = false;
    private int tickDelay = 10; // 1 second delay between snapshots
    private int tickCounter = 0;

    private long lastSnapshotTime = 0;

    public RewindController(ServerPlayer player) {
        this.player = player;
    }

    public void tick(long currentTimeMillis) {
        if (isRewinding) return; // Don't record during rewind

        if (currentTimeMillis - lastSnapshotTime >= SNAPSHOT_INTERVAL_MS) {
            WorldSnapshot snapshot = SnapshotManager.captureSnapshot(player);
            rewindBuffer.addLast(snapshot.toNbt());
            lastSnapshotTime = currentTimeMillis;
        }

        // Trim old snapshots
        rewindBuffer.removeIf(tag -> {
            long timestamp = tag.getLong("Timestamp");
            return currentTimeMillis - timestamp > MAX_SNAPSHOT_AGE_MS;
        });
    }

    public void rewind() {
        if (rewindBuffer.isEmpty()) return;

        isRewinding = true;
        tickCounter = 0;
        ClientEvents.lockMovement = true;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isRewinding) return;

        tickCounter++;

        if (tickCounter >= tickDelay) {
            tickCounter = 0;

            if (!rewindBuffer.isEmpty()) {
                CompoundTag tag = rewindBuffer.pollLast();
                WorldSnapshot snapshot = WorldSnapshot.fromNbt(tag);
                SnapshotManager.restoreSnapshot(player, snapshot);
            } else {
                isRewinding = false;
                ClientEvents.lockMovement = false;
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }
}