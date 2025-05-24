package net.mattseq.echoes_of_time.networking;

import net.mattseq.echoes_of_time.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TotemPacket {
    private final boolean shouldActivate;

    public TotemPacket(boolean shouldActivate) {
        this.shouldActivate = shouldActivate;
    }

    public static void encode(TotemPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.shouldActivate);
    }

    public static TotemPacket decode(FriendlyByteBuf buf) {
        return new TotemPacket(buf.readBoolean());
    }

    public static void handle(TotemPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.shouldActivate) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.particleEngine.createTrackingEmitter(Minecraft.getInstance().player, ParticleTypes.TOTEM_OF_UNDYING, 30);
                    mc.level.playLocalSound(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ(), SoundEvents.TOTEM_USE, Minecraft.getInstance().player.getSoundSource(), 1.0F, 1.0F, false);
                    mc.gameRenderer.displayItemActivation(new ItemStack(ModItems.TOTEM_OF_ECHOES.get()));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
