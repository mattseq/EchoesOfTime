package net.mattseq.timemachine;

// File: SnapshotManager.java

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SnapshotManager {
    public static WorldSnapshot captureSnapshot(ServerPlayer player) {
        ServerLevel world = player.serverLevel(); // ServerWorld in 1.20 Forge
        BlockPos center = player.blockPosition();
        List<BlockSnapshot> blocks = new ArrayList<>();
        List<EntitySnapshot> entities = new ArrayList<>();

        int radius = 8;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    blocks.add(new BlockSnapshot(pos, state));
                }
            }
        }

        AABB box = new AABB(center).inflate(radius);
        for (Entity entity : world.getEntities(player, box, e -> e != player)) {
            entities.add(new EntitySnapshot(entity.getType(), entity.position()));
        }

        Vec3 playerPos = player.position();
        float playerHealth = player.getHealth();
        List<ItemStack> inventory = new ArrayList<>(player.getInventory().items);

        long timestamp = System.currentTimeMillis();

        return new WorldSnapshot(playerPos, playerHealth, inventory, blocks, entities, timestamp);
    }

    public static void restoreSnapshot(ServerPlayer player, WorldSnapshot snapshot) {
        ServerLevel world = player.serverLevel();
        BlockPos center = player.blockPosition();
        int radius = 8;

        // === Restore Blocks ===
        for (BlockSnapshot block : snapshot.blocks) {
            world.setBlockAndUpdate(block.pos, block.state);
        }

        // === Clear Existing Entities (excluding player) in Area ===
        AABB area = new AABB(center).inflate(radius);
        List<Entity> entitiesToRemove = world.getEntities(player, area, e -> e != player);
        for (Entity entity : entitiesToRemove) {
            entity.discard(); // safely removes the entity from the world
        }

        // === Restore Entities ===
        for (EntitySnapshot entitySnap : snapshot.entities) {
            Entity entity = entitySnap.type.create(world);
            if (entity != null) {
                entity.setPos(entitySnap.pos.x, entitySnap.pos.y, entitySnap.pos.z);
                world.addFreshEntity(entity);
            }
        }

        // === Restore Player State ===
        player.setPos(snapshot.playerPos.x, snapshot.playerPos.y, snapshot.playerPos.z);
        player.setHealth(snapshot.playerHealth);

        player.getInventory().clearContent();
        List<ItemStack> fullInventory = snapshot.playerInventory;
        if (fullInventory.size() >= 41) {
            List<ItemStack> items = fullInventory.subList(0, 36);
            List<ItemStack> armor = fullInventory.subList(36, 40);
            List<ItemStack> offhand = fullInventory.subList(40, 41);
            for (int i = 0; i < items.size(); i++) {
                player.getInventory().items.set(i, items.get(i));
            }
            for (int i = 0; i < armor.size(); i++) {
                player.getInventory().armor.set(i, armor.get(i));
            }
            for (int i = 0; i < offhand.size(); i++) {
                player.getInventory().offhand.set(i, offhand.get(i));
            }
        } else {
            TimeMachine.LOGGER.warn("Snapshot inventory too small! Skipping restore.");
            return;
        }
        player.inventoryMenu.broadcastChanges(); // syncs inventory with client
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
