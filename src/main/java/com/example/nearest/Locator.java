package com.example.nearest;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.structure.Structure;

import java.util.*;

public final class Locator {
    public record Type(String ru, List<String> ids) {}

    public static final String SPAWNER = "spawner";
    public static final Map<String, Type> TYPES = new LinkedHashMap<>();

    static {
        TYPES.put(SPAWNER, new Type("Спавнер", List.of()));
        TYPES.put("temple", new Type("Храм", List.of("desert_pyramid", "jungle_pyramid")));
        TYPES.put("mineshaft", new Type("Заброшенная шахта", List.of("mineshaft", "mineshaft_mesa")));
        TYPES.put("stronghold", new Type("Крепость (портал в Энд)", List.of("stronghold")));
        TYPES.put("fortress", new Type("Адская крепость", List.of("fortress")));
        TYPES.put("bastion", new Type("Бастион", List.of("bastion_remnant")));
        TYPES.put("village", new Type("Деревня", List.of("village_plains", "village_desert",
                "village_savanna", "village_snowy", "village_taiga")));
        TYPES.put("ancient_city", new Type("Древний город", List.of("ancient_city")));
        TYPES.put("trial_chambers", new Type("Камеры испытаний", List.of("trial_chambers")));
        TYPES.put("monument", new Type("Океанский монумент", List.of("ocean_monument")));
        TYPES.put("mansion", new Type("Лесной особняк", List.of("mansion")));
        TYPES.put("outpost", new Type("Застава разбойников", List.of("pillager_outpost")));
    }

    private Locator() {}

    public static BlockPos locate(ServerWorld world, BlockPos origin, String key) {
        if (SPAWNER.equals(key)) return findSpawner(world, origin);
        Type t = TYPES.get(key);
        return t == null ? null : findStructure(world, origin, t.ids());
    }

    private static BlockPos findStructure(ServerWorld world, BlockPos origin, List<String> ids) {
        Registry<Structure> reg = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        List<RegistryEntry<Structure>> entries = new ArrayList<>();
        for (String id : ids) {
            reg.getOptional(RegistryKey.of(RegistryKeys.STRUCTURE, Identifier.ofVanilla(id)))
                    .ifPresent(entries::add);
        }
        if (entries.isEmpty()) return null;
        Pair<BlockPos, RegistryEntry<Structure>> res = world.getChunkManager().getChunkGenerator()
                .locateStructure(world, RegistryEntryList.of(entries), origin, 100, false);
        return res == null ? null : res.getFirst();
    }

    /** Спавнеры — блоки, а не структуры: ищем в уже загруженных чанках. */
    private static BlockPos findSpawner(ServerWorld world, BlockPos origin) {
        int radius = Math.min(world.getServer().getPlayerManager().getViewDistance(), 32);
        ChunkPos c = new ChunkPos(origin);
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk chunk = world.getChunk(c.x + dx, c.z + dz, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                for (BlockPos p : chunk.getBlockEntityPositions()) {
                    if (chunk.getBlockEntity(p) instanceof MobSpawnerBlockEntity) {
                        double d = p.getSquaredDistance(origin);
                        if (d < bestD) { bestD = d; best = p.toImmutable(); }
                    }
                }
            }
        }
        return best;
    }
}
