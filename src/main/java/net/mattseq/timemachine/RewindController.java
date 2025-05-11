package net.mattseq.timemachine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.Deque;

public class RewindController {
    private static final long MAX_SNAPSHOT_AGE_MS = 10000; // Store 10 seconds of snapshots
    private static final long SNAPSHOT_INTERVAL_MS = 500;  // Take a snapshot every 0.5 seconds

    private final ServerPlayer player;
    private final Deque<CompoundTag> rewindBuffer = new ArrayDeque<>();

    private long lastSnapshotTime = 0;

    public RewindController(ServerPlayer player) {
        this.player = player;
    }

    public void tick(long currentTimeMillis) {
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
        while (!rewindBuffer.isEmpty()) {
            // Pause before restoring each snapshot
//            try {
//                Thread.sleep(2000); // Delay
//            } catch (InterruptedException e) {
//                // Handle the exception if the thread is interrupted
//                Thread.currentThread().interrupt();
//                return;
//            }

            CompoundTag tag = rewindBuffer.pollLast(); // Newest first
            WorldSnapshot snapshot = WorldSnapshot.fromNbt(tag); // Deserialize
            SnapshotManager.restoreSnapshot(player, snapshot);
        }
    }
}