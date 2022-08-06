package me.zailer.plotcubic.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PlotworldGenerator extends ChunkGenerator {
    public static final Codec<PlotworldGenerator> CODEC = RecordCodecBuilder.create((instance) ->
            createStructureSetRegistryGetter(instance)
                    .and(RegistryOps.createRegistryCodec(Registry.BIOME_KEY).forGetter((generator) -> generator.biomeRegistry))
                    .and(RegistryOps.createRegistryCodec(Registry.DIMENSION_TYPE_KEY).forGetter((generator) -> generator.registryDimensionType))
                    .apply(instance, instance.stable(PlotworldGenerator::new)));

    private final Registry<Biome> biomeRegistry;
    private final Registry<DimensionType> registryDimensionType;
    private final PlotManager plotManager;

    public PlotworldGenerator(Registry<Biome> biomeRegistry, Biome biome, Registry<DimensionType> registryDimensionType) {
        super(getEmptyStructureRegistry(),
                Optional.empty(),
                getBiomeSource(biomeRegistry, biome),
                biomeRegistryEntry -> new GenerationSettings.Builder().build()
        );
        this.biomeRegistry = biomeRegistry;
        this.plotManager = PlotManager.getInstance();
        this.registryDimensionType = registryDimensionType;

        DimensionType dimensionType = this.registryDimensionType.get(PlotCubic.PLOTWORLD_DIMENSION_TYPE);
        if (dimensionType != null)
            this.plotManager.setHeight(dimensionType.minY(), dimensionType.height());
    }

    public static FixedBiomeSource getBiomeSource(Registry<Biome> biomeRegistry, Biome biome) {
        Optional<RegistryKey<Biome>> registryKey = biomeRegistry.getKey(biome);
        RegistryEntry<Biome> registryEntry = biomeRegistry.getOrCreateEntry(registryKey.orElse(BiomeKeys.PLAINS));

        return new FixedBiomeSource(registryEntry);
    }

    public PlotworldGenerator(Registry<StructureSet> structureSets, Registry<Biome> biomeRegistry, Registry<DimensionType> registryDimensionType) {
        this(biomeRegistry, PlotManager.getInstance().getBiome(), registryDimensionType);
    }

    @SuppressWarnings("unchecked")
    public static Registry<StructureSet> getEmptyStructureRegistry() {
        MutableRegistry<StructureSet> structureSets = (MutableRegistry<StructureSet>) DynamicRegistryManager.createSimpleRegistry(Registry.STRUCTURE_SET_KEY);
        structureSets.clearTags();
        return structureSets;
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
        for (int x = 0; x != SharedConstants.CHUNK_WIDTH; x++) {
            for (int z = 0; z != SharedConstants.CHUNK_WIDTH; z++) {
                this.regen(chunk, x, z);
            }
        }
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    public void regen(Chunk chunk, int x, int z) {
        for (int y = this.getMinimumY(); y != this.getMaxHeight(); y++) {
            BlockState block = this.plotManager.getBlock(chunk.getPos().getBlockPos(x, y, z));
            chunk.setBlockState(new BlockPos(x, y, z), block, false);
        }
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return this.getMinimumY();
    }

    @Override
    public int getWorldHeight() {
        return this.plotManager.getHeight();
    }

    public int getMaxHeight() {
        return this.plotManager.getMaxHeight();
    }

    @Override
    public int getMinimumY() {
        return this.plotManager.getMinHeight();
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return this.plotManager.getMaxTerrainHeight();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(this.getMinimumY(), new BlockState[0]);
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {

    }

    public static PlotworldGenerator createPlotGenerator(MinecraftServer server) {
        DynamicRegistryManager registryManager = server.getRegistryManager();
        return new PlotworldGenerator(registryManager.get(Registry.BIOME_KEY),
                PlotManager.getInstance().getBiome(),
                registryManager.get(Registry.DIMENSION_TYPE_KEY)
        );
    }
}
