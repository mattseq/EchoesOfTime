package net.mattseq.echoes_of_time;

import net.mattseq.echoes_of_time.events.ClientEvents;
import net.mattseq.echoes_of_time.snapshots.WorldSnapshot;
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
    private static final long REWIND_SPEED = 100; // 100 ms of rewind time per 1 tick (50 ms) of real time

    private final ServerPlayer player;
    private final Deque<CompoundTag> rewindBuffer = new ArrayDeque<>();

    public boolean isRewinding = false;

    private long rewindCurrentTime;
    private long rewindTargetTime;

    private CompoundTag cachedPrevTag;
    private WorldSnapshot cachedPrevSnapshot;

    private CompoundTag cachedNextTag;
    private WorldSnapshot cachedNextSnapshot;

    private long lastSnapshotTime = 0;

    public RewindController(ServerPlayer player) {
        this.player = player;
    }

    public void tick(long currentTimeMillis) {
        if (isRewinding) return; // Don't record during rewind

        // record
        if (currentTimeMillis - lastSnapshotTime >= SNAPSHOT_INTERVAL_MS) {
            WorldSnapshot snapshot = SnapshotManager.captureSnapshot(player);
            rewindBuffer.addLast(snapshot.toNbt());
            lastSnapshotTime = currentTimeMillis;
        }

        // Trim old snapshots
        while (rewindBuffer.size() > (MAX_SNAPSHOT_AGE_MS/SNAPSHOT_INTERVAL_MS)) {
            rewindBuffer.removeFirst(); // remove the oldest snapshot
        }
    }

    public void startRewind(float percent) {
        if (rewindBuffer.isEmpty()) return;

        long latestTime = rewindBuffer.getLast().getLong("Timestamp");
        rewindCurrentTime = latestTime;
        rewindTargetTime = latestTime - (long)(percent * MAX_SNAPSHOT_AGE_MS);
        isRewinding = true;
        ClientEvents.lockMovement = true;
        player.setInvulnerable(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void stopRewind() {
        isRewinding = false;
        rewindBuffer.clear();
        ClientEvents.lockMovement = false;
        player.setInvulnerable(false);

        // Clear cached snapshots
        cachedPrevTag = null;
        cachedPrevSnapshot = null;
        cachedNextTag = null;
        cachedNextSnapshot = null;

        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isRewinding) return;

        rewindCurrentTime -= REWIND_SPEED;

        if (rewindCurrentTime <= rewindTargetTime || rewindBuffer.isEmpty()) {
            stopRewind();
            return;
        }

        CompoundTag prevTag = null;
        CompoundTag nextTag = null;

        long prevTime = 0;
        long nextTime = 0;

        for (CompoundTag tag : rewindBuffer) {
            long ts = tag.getLong("Timestamp");
            if (ts <= rewindCurrentTime) {
                prevTag = tag;  // update prev until no longer <= rewindCurrentTime
                prevTime = ts;
            } else {
                nextTag = tag;  // first tag > rewindCurrentTime is next
                nextTime = ts;
                break;
            }
        }

        if (prevTag == null) {
            // first rewind
            WorldSnapshot snapshot = WorldSnapshot.fromNbt(nextTag);
            SnapshotManager.restoreSnapshot(player, snapshot);
            return;
        } else if (nextTag == null) {
            // last rewind
            WorldSnapshot snapshot = WorldSnapshot.fromNbt(prevTag);
            SnapshotManager.restoreSnapshot(player, snapshot);
            return;
        }

        // cache snapshots for interpolation
        if (cachedPrevTag != prevTag) {
            cachedPrevTag = prevTag;
            cachedPrevSnapshot = WorldSnapshot.fromNbt(prevTag);
        }

        if (cachedNextTag != nextTag) {
            cachedNextTag = nextTag;
            cachedNextSnapshot = WorldSnapshot.fromNbt(nextTag);
        }

        float alpha = (float) (rewindCurrentTime - prevTime) / (nextTime - prevTime);

        WorldSnapshot interpolatedSnapshot = WorldSnapshot.interpolate(
                cachedPrevSnapshot, cachedNextSnapshot,
                alpha);

        SnapshotManager.restoreSnapshot(player, interpolatedSnapshot);


    }
}