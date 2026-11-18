package com.example.nearest;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Клиент -> сервер: "найди ближайшее <type>". */
public record RequestPayload(String type) implements CustomPayload {
    public static final Id<RequestPayload> ID = new Id<>(Identifier.of("nearest_structures", "request"));
    public static final PacketCodec<RegistryByteBuf, RequestPayload> CODEC =
            PacketCodecs.STRING.xmap(RequestPayload::new, RequestPayload::type).cast();

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
