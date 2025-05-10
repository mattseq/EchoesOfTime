package net.mattseq.timemachine.item;

import net.mattseq.timemachine.SnapshotManager;
import net.mattseq.timemachine.TimeMachine;
import net.mattseq.timemachine.WorldSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RestoreWandItem extends Item {
    public RestoreWandItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Path filePath = Path.of("snapshots", serverPlayer.getScoreboardName() + ".nbt");
            if (!Files.exists(filePath)) {
                player.sendSystemMessage(Component.literal("No snapshot found."));
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            try {
                WorldSnapshot snapshot = SnapshotManager.loadSnapshotFromFile(filePath);
                SnapshotManager.restoreSnapshot(serverPlayer, snapshot);
                player.sendSystemMessage(Component.literal("Snapshot restored!"));
            } catch (IOException e) {
                player.sendSystemMessage(Component.literal("Failed to load snapshot."));
                e.printStackTrace();
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
