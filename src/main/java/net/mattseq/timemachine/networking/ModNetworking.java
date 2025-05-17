package net.mattseq.timemachine.networking;

import net.mattseq.timemachine.TimeMachine;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(TimeMachine.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, SetViewPacket.class, SetViewPacket::encode, SetViewPacket::decode, SetViewPacket::handle);
        CHANNEL.registerMessage(packetId++, TotemPacket.class, TotemPacket::encode, TotemPacket::decode, TotemPacket::handle);
    }
}
