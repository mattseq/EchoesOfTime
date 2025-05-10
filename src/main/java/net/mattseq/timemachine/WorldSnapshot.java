package net.mattseq.timemachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class WorldSnapshot {

    public Vec3 playerPos;
    public float playerHealth;
    public List<ItemStack> playerInventory;

    public int experience;
    public int foodLevel;
    public float saturationLevel;
    public List<MobEffectInstance> effects;


    public List<BlockSnapshot> blocks;
    public List<EntitySnapshot> entities;

    public long worldTime;
    public boolean isRaining;
    public boolean isThundering;

    public long timestamp;

    public WorldSnapshot(ServerPlayer player, List<BlockSnapshot> blocks, List<EntitySnapshot> entities, long timestamp) {

        this.playerPos = player.position();
        this.playerHealth = player.getHealth();
        this.playerInventory = new ArrayList<>();
        this.playerInventory.addAll(player.getInventory().items);
        this.playerInventory.addAll(player.getInventory().armor);
        this.playerInventory.addAll(player.getInventory().offhand);

        this.experience = player.totalExperience;
        this.foodLevel = player.getFoodData().getFoodLevel();
        this.saturationLevel = player.getFoodData().getSaturationLevel();
        this.effects = new ArrayList<>(player.getActiveEffects());


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
            Vec3 playerPos, float playerHealth, int experience, int foodLevel, float saturationLevel,
            List<ItemStack> playerInventory, List<MobEffectInstance> effects,
            long worldTime, boolean isRaining, boolean isThundering, long timestamp) {
        this.blocks = blocks;
        this.entities = entities;
        this.playerPos = playerPos;
        this.playerHealth = playerHealth;
        this.experience = experience;
        this.foodLevel = foodLevel;
        this.saturationLevel = saturationLevel;
        this.playerInventory = playerInventory;
        this.timestamp = timestamp;
        this.effects = effects;
        this.worldTime = worldTime;
        this.isRaining = isRaining;
        this.isThundering = isThundering;
    }


    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        tag.putDouble("PosX", playerPos.x);
        tag.putDouble("PosY", playerPos.y);
        tag.putDouble("PosZ", playerPos.z);
        tag.putFloat("Health", playerHealth);

        tag.putInt("Experience", experience);
        tag.putInt("FoodLevel", foodLevel);
        tag.putFloat("Saturation", saturationLevel);

        // Effects
        ListTag effectsTag = new ListTag();
        for (MobEffectInstance effect : effects) {
            effectsTag.add(effect.save(new CompoundTag()));
        }
        tag.put("Effects", effectsTag);

        // Inventory
        ListTag invTag = new ListTag();
        for (ItemStack stack : playerInventory) {
            invTag.add(stack.save(new CompoundTag()));
        }
        tag.put("Inventory", invTag);

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

        float health = tag.getFloat("Health");
        int experience = tag.getInt("Experience");
        int foodLevel = tag.getInt("FoodLevel");
        float saturation = tag.getFloat("Saturation");
        long worldTime = tag.getLong("WorldTime");
        boolean isRaining = tag.getBoolean("IsRaining");
        boolean isThundering = tag.getBoolean("IsThundering");
        long timestamp = tag.getLong("Timestamp");

        // Effects
        List<MobEffectInstance> effects = new ArrayList<>();
        ListTag effectsTag = tag.getList("Effects", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < effectsTag.size(); i++) {
            effects.add(MobEffectInstance.load(effectsTag.getCompound(i)));
        }

        // Inventory
        ListTag invTag = tag.getList("Inventory", CompoundTag.TAG_COMPOUND);
        List<ItemStack> inventory = new java.util.ArrayList<>();
        for (int i = 0; i < invTag.size(); i++) {
            inventory.add(ItemStack.of(invTag.getCompound(i)));
        }

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
                playerPos, health, experience, foodLevel, saturation, inventory, effects,
                worldTime, isRaining, isThundering, timestamp);
    }

}
