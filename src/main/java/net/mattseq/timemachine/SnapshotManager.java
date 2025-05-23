package net.mattseq.timemachine;

// File: SnapshotManager.java

import net.mattseq.timemachine.networking.ModNetworking;
import net.mattseq.timemachine.networking.SetViewPacket;
import net.mattseq.timemachine.snapshots.BlockSnapshot;
import net.mattseq.timemachine.snapshots.EntitySnapshot;
import net.mattseq.timemachine.snapshots.WorldSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SnapshotManager {

    public static final int radius = 8;

    public static WorldSnapshot captureSnapshot(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos center = player.blockPosition();
        List<BlockSnapshot> blocks = new ArrayList<>();
        List<EntitySnapshot> entities = new ArrayList<>();


        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    CompoundTag blockEntityData = state.hasBlockEntity() ? world.getBlockEntity(pos).saveWithFullMetadata() : null;
                    blocks.add(new BlockSnapshot(pos, state, blockEntityData));
                }
            }
        }

        AABB box = new AABB(center).inflate(radius);
        for (Entity entity : world.getEntities(player, box, e -> e != player)) {
            entities.add(EntitySnapshot.fromEntity(entity));
        }

        long timestamp = System.currentTimeMillis();

        CompoundTag playerTag = new CompoundTag();
        player.saveWithoutId(playerTag);

        return new WorldSnapshot(player, blocks, entities, timestamp);
    }

    public static void restoreSnapshot(ServerPlayer player, WorldSnapshot snapshot) {
        ServerLevel world = player.serverLevel();
        BlockPos center = new BlockPos((int) snapshot.playerPos.x, (int) snapshot.playerPos.y, (int) snapshot.playerPos.z);

        // === Restore Blocks ===
        for (BlockSnapshot block : snapshot.blocks) {
            world.setBlockAndUpdate(block.pos, block.state);
            if (block.blockEntityData != null) {
                Objects.requireNonNull(world.getBlockEntity(block.pos)).load(block.blockEntityData);
            }
        }

        // === Clear Existing Entities (excluding player) in Area ===
        AABB area = new AABB(center).inflate(radius);
        List<Entity> entitiesToRemove = world.getEntities(player, area, e -> e != player);
        for (Entity entity : entitiesToRemove) {
            entity.discard(); // safely removes the entity from the world
        }

        // === Restore Entities ===
        for (EntitySnapshot entitySnap : snapshot.entities) {
            // Create the entity from its NBT data (using the saved data)
            CompoundTag entityData = entitySnap.entityData;
            String entityTypeName = entityData.getString("id");  // This is the entity's ID (used to determine its type)

            EntityType<?> entityType = EntityType.byString(entityTypeName).orElse(null);

            if (entityType != null) {
                // Create the entity using its type
                Optional<Entity> optionalEntity = Optional.ofNullable(entityType.create(world));

                optionalEntity.ifPresent(entity -> {
                    // Load the entity's NBT data
                    entity.load(entityData);

                    // Add the entity to the world without modifying its position
                    world.addFreshEntity(entity);
                });
            }
        }

        player.load(snapshot.playerData);
        player.teleportTo(snapshot.playerPos.x, snapshot.playerPos.y, snapshot.playerPos.z);
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SetViewPacket(snapshot.playerYaw, snapshot.playerPitch));
        player.inventoryMenu.broadcastChanges();
    }


    public static void saveSnapshotToFile(WorldSnapshot snapshot, Path filePath) throws IOException {
        CompoundTag nbt = snapshot.toNbt();
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(filePath))) {
            NbtIo.writeCompressed(nbt, out);
        }
    }

    public static WorldSnapshot loadSnapshotFromFile(Path filePath) throws IOException {
        try (var in = Files.newInputStream(filePath)) {
            CompoundTag tag = NbtIo.readCompressed(in);
            return WorldSnapshot.fromNbt(tag);
        }
    }

}
