package net.mattseq.timemachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSnapshot {
    public BlockPos pos;
    public BlockState state;

    public BlockSnapshot(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }
}
