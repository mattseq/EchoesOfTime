package net.mattseq.timemachine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WorldSnapshot {
    public Vec3 playerPos;
    public float playerHealth;
    public List<ItemStack> playerInventory;
    public List<BlockSnapshot> blocks;
    public List<EntitySnapshot> entities;
    public long timestamp;

    public WorldSnapshot(ServerPlayer player, List<BlockSnapshot> blocks, List<EntitySnapshot> entities, long timestamp) {
        this.playerPos = player.position();
        this.playerHealth = player.getHealth();
        this.playerInventory.addAll(player.getInventory().items);
        this.playerInventory.addAll(player.getInventory().armor);
        this.playerInventory.addAll(player.getInventory().offhand);

        this.blocks = blocks;
        this.entities = entities;
        this.timestamp = timestamp;
    }

    public WorldSnapshot(Vec3 playerPos, float playerHealth,
                         List<ItemStack> playerInventory, List<BlockSnapshot> blocks,
                         List<EntitySnapshot> entities, long timestamp) {
        this.playerPos = playerPos;
        this.playerHealth = playerHealth;
        this.playerInventory = playerInventory;
        this.blocks = blocks;
        this.entities = entities;
        this.timestamp = timestamp;
    }


    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        tag.putDouble("PosX", playerPos.x);
        tag.putDouble("PosY", playerPos.y);
        tag.putDouble("PosZ", playerPos.z);
        tag.putFloat("Health", playerHealth);
        tag.putLong("Timestamp", timestamp);

        // Inventory
        ListTag invTag = new ListTag();
        for (ItemStack stack : playerInventory) {
            invTag.add(stack.save(new CompoundTag()));
        }
        tag.put("Inventory", invTag);

        // Blocks
        ListTag blocksTag = new ListTag();
        for (BlockSnapshot bs : blocks) {
            CompoundTag b = new CompoundTag();
            b.putInt("X", bs.pos.getX());
            b.putInt("Y", bs.pos.getY());
            b.putInt("Z", bs.pos.getZ());
            b.put("State", NbtUtils.writeBlockState(bs.state));
            blocksTag.add(b);
        }
        tag.put("Blocks", blocksTag);

        // Entities
        ListTag entitiesTag = new ListTag();
        for (EntitySnapshot es : entities) {
            CompoundTag e = new CompoundTag();
            e.putString("Type", EntityType.getKey(es.type).toString());
            e.putDouble("X", es.pos.x);
            e.putDouble("Y", es.pos.y);
            e.putDouble("Z", es.pos.z);
            entitiesTag.add(e);
        }
        tag.put("Entities", entitiesTag);

        return tag;
    }

    public static WorldSnapshot fromNbt(CompoundTag tag) {
        double x = tag.getDouble("PosX");
        double y = tag.getDouble("PosY");
        double z = tag.getDouble("PosZ");
        Vec3 playerPos = new Vec3(x, y, z);

        float health = tag.getFloat("Health");
        long timestamp = tag.getLong("Timestamp");

        // Inventory
        ListTag invTag = tag.getList("Inventory", CompoundTag.TAG_COMPOUND);
        List<ItemStack> inventory = new java.util.ArrayList<>();
        for (int i = 0; i < invTag.size(); i++) {
            inventory.add(ItemStack.of(invTag.getCompound(i)));
        }

        // Blocks
        ListTag blocksTag = tag.getList("Blocks", CompoundTag.TAG_COMPOUND);
        List<BlockSnapshot> blocks = new java.util.ArrayList<>();
        for (int i = 0; i < blocksTag.size(); i++) {
            CompoundTag b = blocksTag.getCompound(i);
            BlockPos pos = new BlockPos(b.getInt("X"), b.getInt("Y"), b.getInt("Z"));
            BlockState state = NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), b.getCompound("State"));
            blocks.add(new BlockSnapshot(pos, state));
        }

        // Entities
        ListTag entitiesTag = tag.getList("Entities", CompoundTag.TAG_COMPOUND);
        List<EntitySnapshot> entities = new java.util.ArrayList<>();
        for (int i = 0; i < entitiesTag.size(); i++) {
            CompoundTag e = entitiesTag.getCompound(i);
            EntityType<?> type = EntityType.byString(e.getString("Type")).orElse(null);
            if (type != null) {
                Vec3 pos = new Vec3(e.getDouble("X"), e.getDouble("Y"), e.getDouble("Z"));
                entities.add(new EntitySnapshot(type, pos));
            }
        }

        return new WorldSnapshot(playerPos, health, inventory, blocks, entities, timestamp);
    }

}
