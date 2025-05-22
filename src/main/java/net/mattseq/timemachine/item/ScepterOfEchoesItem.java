package net.mattseq.timemachine.item;

import net.mattseq.timemachine.RewindController;
import net.mattseq.timemachine.RewindGlobalManager;
import net.mattseq.timemachine.TimeMachine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ScepterOfEchoesItem extends Item {

    public ScepterOfEchoesItem(Properties p_41383_) {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(64) // Set durability here
                .rarity(Rarity.RARE));
    }

    // Called when the player starts using the item (right-click)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    // How long the item can be used (charge time in ticks)
    @Override
    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    // show charging animation like a bow
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    // Called when the player releases right-click
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (!(user instanceof ServerPlayer player) || level.isClientSide()) return;

        int chargeTime = getUseDuration(stack) - timeLeft; // ticks held
        float chargePercent = (float) chargeTime / getUseDuration(stack);

        RewindController controller = RewindGlobalManager.getOrCreateController(player); // Assuming you have a per-player controller
        controller.startRewind(chargePercent);

        // damage the item slightly
        stack.hurt(1, level.getRandom(), player);
    }
}
