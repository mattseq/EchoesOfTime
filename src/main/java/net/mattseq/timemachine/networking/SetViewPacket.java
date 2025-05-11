package net.mattseq.timemachine.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetViewPacket {
    private final float yaw;
    private final float pitch;

    public SetViewPacket(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(SetViewPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.yaw);
        buf.writeFloat(packet.pitch);
    }

    public static SetViewPacket decode(FriendlyByteBuf buf) {
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        return new SetViewPacket(yaw, pitch);
    }

    public static void handle(SetViewPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            // Runs on client only
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.setYRot(packet.yaw);
                mc.player.setXRot(packet.pitch);
                mc.player.yRotO = packet.yaw;
                mc.player.xRotO = packet.pitch;
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}

