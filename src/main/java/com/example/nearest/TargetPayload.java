package com.example.nearest;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Сервер -> клиент: результат поиска. */
public record TargetPayload(boolean found, String type, String dim, int x, int y, int z) implements CustomPayload {
    public static final Id<TargetPayload> ID = new Id<>(Identifier.of("nearest_structures", "target"));
    public static final PacketCodec<RegistryByteBuf, TargetPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, TargetPayload::found,
            PacketCodecs.STRING, TargetPayload::type,
            PacketCodecs.STRING, TargetPayload::dim,
            PacketCodecs.INTEGER, TargetPayload::x,
            PacketCodecs.INTEGER, TargetPayload::y,
            PacketCodecs.INTEGER, TargetPayload::z,
            TargetPayload::new).cast();

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
