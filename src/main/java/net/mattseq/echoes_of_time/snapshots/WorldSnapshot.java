package net.mattseq.echoes_of_time.snapshots;

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
    public float playerPitch;
    public float playerYaw;

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
        this.playerPitch = player.getXRot();
        this.playerYaw = player.getYRot();

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
            Vec3 playerPos, float playerPitch, float playerYaw, CompoundTag playerData,
            long worldTime, boolean isRaining, boolean isThundering, long timestamp) {
        this.blocks = blocks;
        this.entities = entities;
        this.playerPos = playerPos;
        this.playerPitch = playerPitch;
        this.playerYaw = playerYaw;
        this.playerData = playerData;
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

        tag.putFloat("XRot", playerPitch);
        tag.putFloat("YRot", playerYaw);

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

        float playerPitch = tag.getFloat("XRot");
        float playerYaw = tag.getFloat("YRot");

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
                playerPos, playerPitch, playerYaw, playerData,
                worldTime, isRaining, isThundering, timestamp);
    }


    public static WorldSnapshot interpolate(WorldSnapshot a, WorldSnapshot b, float alpha) {
        // === Interpolate player position ===
        Vec3 interpolatedPos = new Vec3(
                lerp(a.playerPos.x, b.playerPos.x, alpha),
                lerp(a.playerPos.y, b.playerPos.y, alpha),
                lerp(a.playerPos.z, b.playerPos.z, alpha)
        );

        // === Interpolate rotation (yaw/pitch) ===
        float interpolatedYaw = lerpAngle(a.playerYaw, b.playerYaw, alpha);
        float interpolatedPitch = lerpAngle(a.playerPitch, b.playerPitch, alpha);

        // === Choose weather and time from a, or interpolate if desired ===
        long interpolatedTime = (long) (a.worldTime + alpha * (b.worldTime - a.worldTime));
        boolean interpolatedRain = a.isRaining;     // optional: blend
        boolean interpolatedThunder = a.isThundering;

        // === Use blocks/entities/playerData from snapshot a (cheaper than interpolating) ===
        return new WorldSnapshot(
                a.blocks,
                a.entities,
                interpolatedPos,
                interpolatedPitch,
                interpolatedYaw,
                a.playerData,
                interpolatedTime,
                interpolatedRain,
                interpolatedThunder,
                (long) (a.timestamp + alpha * (b.timestamp - a.timestamp))
        );
    }

    private static double lerp(double a, double b, float alpha) {
        return a + alpha * (b - a);
    }

    private static float lerpAngle(float a, float b, float alpha) {
        float delta = ((b - a + 540f) % 360f) - 180f;
        return a + alpha * delta;
    }


}
