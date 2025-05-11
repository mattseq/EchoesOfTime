package net.mattseq.timemachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class WorldSnapshot {

    public CompoundTag playerData;
    public Vec3 playerPos;

    public List<BlockSnapshot> blocks;
    public List<EntitySnapshot> entities;

    public long worldTime;
    public boolean isRaining;
    public boolean isThundering;

    public long timestamp;

    public WorldSnapshot(ServerPlayer player, List<BlockSnapshot> blocks, List<EntitySnapshot> entities, long timestamp) {

        CompoundTag tag = new CompoundTag();
        player.saveWithoutId(tag);
        this.playerData = tag;
        this.playerPos = player.position();

        this.blocks = blocks;
        this.entities = entities;

        this.worldTime = player.level().getDayTime();
        this.isRaining = player.level().isRaining();
        this.isThundering = player.level().isThundering();

        this.timestamp = timestamp;
    }

    // second constructor for creating WorldSnapshot from nbt
    public WorldSnapshot(
            List<BlockSnapshot> blocks, List<EntitySnapshot> entities,
            Vec3 playerPos, CompoundTag playerData,
            long worldTime, boolean isRaining, boolean isThundering, long timestamp) {
        this.blocks = blocks;
        this.entities = entities;
        this.playerData = playerData;
        this.playerPos = playerPos;
        this.worldTime = worldTime;
        this.isRaining = isRaining;
        this.isThundering = isThundering;
        this.timestamp = timestamp;
    }


    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        tag.putDouble("PosX", playerPos.x);
        tag.putDouble("PosY", playerPos.y);
        tag.putDouble("PosZ", playerPos.z);

        tag.put("PlayerData", playerData);

        // World state
        tag.putLong("WorldTime", worldTime);
        tag.putBoolean("IsRaining", isRaining);
        tag.putBoolean("IsThundering", isThundering);
        tag.putLong("Timestamp", timestamp);

        // Blocks + Block Entities
        ListTag blocksTag = new ListTag();
        for (BlockSnapshot bs : blocks) {
            CompoundTag b = new CompoundTag();
            b.putInt("X", bs.pos.getX());
            b.putInt("Y", bs.pos.getY());
            b.putInt("Z", bs.pos.getZ());
            b.put("State", NbtUtils.writeBlockState(bs.state));
            if (bs.blockEntityData != null) {
                b.put("BlockEntity", bs.blockEntityData);
            }
            blocksTag.add(b);
        }
        tag.put("Blocks", blocksTag);

        // Entities (full NBT)
        ListTag entitiesTag = new ListTag();
        for (EntitySnapshot es : entities) {
            entitiesTag.add(es.entityData);
        }
        tag.put("Entities", entitiesTag);

        return tag;
    }

    public static WorldSnapshot fromNbt(CompoundTag tag) {
        Vec3 playerPos = new Vec3(
                tag.getDouble("PosX"),
                tag.getDouble("PosY"),
                tag.getDouble("PosZ")
        );

        CompoundTag playerData = tag.getCompound("PlayerData");

        long worldTime = tag.getLong("WorldTime");
        boolean isRaining = tag.getBoolean("IsRaining");
        boolean isThundering = tag.getBoolean("IsThundering");
        long timestamp = tag.getLong("Timestamp");

        // Blocks
        List<BlockSnapshot> blocks = new ArrayList<>();
        ListTag blocksTag = tag.getList("Blocks", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < blocksTag.size(); i++) {
            CompoundTag b = blocksTag.getCompound(i);
            BlockPos pos = new BlockPos(b.getInt("X"), b.getInt("Y"), b.getInt("Z"));
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), b.getCompound("State"));
            CompoundTag beData = b.contains("BlockEntity") ? b.getCompound("BlockEntity") : null;
            blocks.add(new BlockSnapshot(pos, state, beData));
        }

        // Entities
        List<EntitySnapshot> entities = new ArrayList<>();
        ListTag entitiesTag = tag.getList("Entities", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < entitiesTag.size(); i++) {
            entities.add(new EntitySnapshot(entitiesTag.getCompound(i)));
        }

        // Build snapshot
        return new WorldSnapshot(
                blocks, entities,
                playerPos, playerData,
                worldTime, isRaining, isThundering, timestamp);
    }

}
