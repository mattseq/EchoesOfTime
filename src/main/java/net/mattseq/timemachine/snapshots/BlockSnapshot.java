package net.mattseq.timemachine.snapshots;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSnapshot {
    public BlockPos pos;
    public BlockState state;
    public CompoundTag blockEntityData;

    // Constructor
    public BlockSnapshot(BlockPos pos, BlockState state, CompoundTag blockEntityData) {
        this.pos = pos;
        this.state = state;
        this.blockEntityData = blockEntityData;
    }
}
