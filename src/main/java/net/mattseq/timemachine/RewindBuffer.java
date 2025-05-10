package net.mattseq.timemachine;

import java.util.ArrayDeque;
import java.util.Deque;

public class RewindBuffer {
    private static final long MAX_AGE_MS = 20_000; // 20 seconds
    private static final int MAX_SNAPSHOTS = 400; // Optional cap (e.g., 2 per second for 20s)

    private final Deque<WorldSnapshot> buffer = new ArrayDeque<>();

    public void add(WorldSnapshot snapshot) {
        buffer.addLast(snapshot);
        trim(snapshot.timestamp);
    }

    private void trim(long now) {
        while (!buffer.isEmpty() && (now - buffer.peekFirst().timestamp > MAX_AGE_MS)) {
            buffer.removeFirst();
        }
        while (buffer.size() > MAX_SNAPSHOTS) {
            buffer.removeFirst();
        }
    }

    public void clear() {
        buffer.clear();
    }

    public Deque<WorldSnapshot> getBuffer() {
        return buffer;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }
}
