package com.example.nearest;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class NearestMod implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(RequestPayload.ID, RequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TargetPayload.ID, TargetPayload.CODEC);

        // запрос из меню / по кнопке
        ServerPlayNetworking.registerGlobalReceiver(RequestPayload.ID, (payload, context) ->
                handle(context.server(), context.player(), payload.type()));

        // команда /nearest <тип> тоже работает (результат так же появится на экране)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("nearest")
                        .then(CommandManager.argument("type", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(Locator.TYPES.keySet(), b))
                                .executes(ctx -> {
                                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                    handle(ctx.getSource().getServer(), p, StringArgumentType.getString(ctx, "type"));
                                    return 1;
                                }))));
    }

    private static void handle(MinecraftServer server, ServerPlayerEntity player, String type) {
        if (!Locator.TYPES.containsKey(type)) return;
        ServerWorld world = worldOf(server, player);
        if (world == null) return;
        BlockPos found = Locator.locate(world, player.getBlockPos(), type);
        String dim = world.getRegistryKey().getValue().toString();
        if (found == null) {
            ServerPlayNetworking.send(player, new TargetPayload(false, type, dim, 0, 0, 0));
            player.sendMessage(Text.literal("Не найдено: " + Locator.TYPES.get(type).ru()
                    + (Locator.SPAWNER.equals(type) ? " (ищу только в загруженных чанках)" : "")), false);
        } else {
            ServerPlayNetworking.send(player, new TargetPayload(true, type, dim,
                    found.getX(), found.getY(), found.getZ()));
        }
    }

    // найти мир игрока (если у тебя компилируется player.getEntityWorld() — можно заменить на него)
    private static ServerWorld worldOf(MinecraftServer server, ServerPlayerEntity p) {
        for (ServerWorld w : server.getWorlds()) {
            if (w.getPlayers().contains(p)) return w;
        }
        return null;
    }
}
