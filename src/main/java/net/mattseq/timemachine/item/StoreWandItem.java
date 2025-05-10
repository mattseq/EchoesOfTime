package net.mattseq.timemachine.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StoreWandItem extends Item {
    public StoreWandItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {



//            WorldSnapshot snapshot = SnapshotManager.captureSnapshot(serverPlayer);
//            Path filePath = Path.of("snapshots", serverPlayer.getScoreboardName() + ".nbt");
//            try {
//                Files.createDirectories(filePath.getParent());
//                SnapshotManager.saveSnapshotToFile(snapshot, filePath);
//                player.sendSystemMessage(Component.literal("Snapshot saved!"));
//            } catch (IOException e) {
//                player.sendSystemMessage(Component.literal("Failed to save snapshot."));
//                e.printStackTrace();
//            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
