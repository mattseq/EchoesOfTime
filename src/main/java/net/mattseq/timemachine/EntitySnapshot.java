package net.mattseq.timemachine;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class EntitySnapshot {
    public EntityType<?> type;
    public Vec3 pos;

    public EntitySnapshot(EntityType<?> type, Vec3 pos) {
        this.type = type;
        this.pos = pos;
    }
}
