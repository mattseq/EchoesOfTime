package net.mattseq.timemachine.snapshots;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class EntitySnapshot {
    public CompoundTag entityData;

    // Constructor
    public EntitySnapshot(CompoundTag entityData) {
        this.entityData = entityData;
    }

    public static EntitySnapshot fromEntity(Entity entity) {
        CompoundTag entityTag = new CompoundTag();
        entity.save(entityTag);
        return new EntitySnapshot(entityTag);
    }
}